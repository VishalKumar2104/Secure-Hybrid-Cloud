package com.sernms.service;

import com.sernms.dto.DeviceDto;
import com.sernms.entity.NetworkDevice;
import com.sernms.entity.NetworkInterface;
import com.sernms.entity.Vlan;
import com.sernms.exception.BadRequestException;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.NetworkDeviceRepository;
import com.sernms.repository.NetworkInterfaceRepository;
import com.sernms.repository.VlanRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final NetworkDeviceRepository deviceRepository;
    private final NetworkInterfaceRepository interfaceRepository;
    private final VlanRepository vlanRepository;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    public DeviceService(NetworkDeviceRepository deviceRepository,
                         NetworkInterfaceRepository interfaceRepository,
                         VlanRepository vlanRepository,
                         AlertService alertService,
                         AuditLogService auditLogService,
                         AuthService authService) {
        this.deviceRepository = deviceRepository;
        this.interfaceRepository = interfaceRepository;
        this.vlanRepository = vlanRepository;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceDto getDeviceById(Long id) {
        NetworkDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Network device not found with id: " + id));
        return mapToDto(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> searchDevices(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDevices();
        }
        return deviceRepository.searchDevices(query.trim()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesByStatus(String status) {
        return deviceRepository.findByStatus(status.toUpperCase()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesByType(String type) {
        return deviceRepository.findByDeviceType(type.toUpperCase()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceDto createDevice(DeviceDto dto, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);

        if (deviceRepository.findByIpAddress(dto.getIpAddress()).isPresent()) {
            throw new BadRequestException("IP address " + dto.getIpAddress() + " is already assigned to another device");
        }

        Vlan vlan = null;
        if (dto.getVlanId() != null) {
            vlan = vlanRepository.findById(dto.getVlanId())
                    .orElseThrow(() -> new BadRequestException("VLAN not found with id: " + dto.getVlanId()));
        }

        NetworkDevice device = new NetworkDevice(
                dto.getName(),
                dto.getDeviceType().toUpperCase(),
                dto.getIpAddress(),
                dto.getMacAddress(),
                dto.getLocation(),
                dto.getManufacturer(),
                dto.getModel(),
                dto.getStatus() != null ? dto.getStatus().toUpperCase() : "ONLINE",
                vlan,
                dto.getDescription()
        );

        NetworkDevice saved = deviceRepository.save(device);

        // Add default interface
        NetworkInterface primaryInterface = new NetworkInterface(
                saved,
                "GigabitEthernet0/1",
                saved.getIpAddress(),
                saved.getMacAddress(),
                "1 Gbps",
                "UP"
        );
        interfaceRepository.save(primaryInterface);

        auditLogService.log(
                authService.getCurrentUsername(),
                "DEVICE_CREATED",
                "NetworkDevice",
                saved.getId().toString(),
                clientIp,
                "SUCCESS",
                "Created device " + saved.getName() + " (" + saved.getIpAddress() + ")"
        );

        return mapToDto(saved);
    }

    @Transactional
    public DeviceDto updateDevice(Long id, DeviceDto dto, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        NetworkDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        device.setName(dto.getName());
        device.setDeviceType(dto.getDeviceType().toUpperCase());
        device.setLocation(dto.getLocation());
        device.setManufacturer(dto.getManufacturer());
        device.setModel(dto.getModel());
        device.setDescription(dto.getDescription());

        if (dto.getVlanId() != null) {
            Vlan vlan = vlanRepository.findById(dto.getVlanId())
                    .orElseThrow(() -> new BadRequestException("VLAN not found with id: " + dto.getVlanId()));
            device.setVlan(vlan);
        }

        NetworkDevice updated = deviceRepository.save(device);

        auditLogService.log(
                authService.getCurrentUsername(),
                "DEVICE_UPDATED",
                "NetworkDevice",
                updated.getId().toString(),
                clientIp,
                "SUCCESS",
                "Updated device attributes for " + updated.getName()
        );

        return mapToDto(updated);
    }

    @Transactional
    public DeviceDto updateDeviceStatus(Long id, String newStatus, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        NetworkDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        String oldStatus = device.getStatus();
        String formattedStatus = newStatus.toUpperCase();
        device.setStatus(formattedStatus);
        device.setLastSeen(LocalDateTime.now());

        NetworkDevice saved = deviceRepository.save(device);

        // Alert condition evaluation
        if ("OFFLINE".equalsIgnoreCase(formattedStatus)) {
            alertService.createAlert(
                    "Network Device Offline: " + device.getName(),
                    "Device " + device.getName() + " at IP " + device.getIpAddress() + " is unreachable. ICMP ping timeout.",
                    "CRITICAL",
                    "NETWORK",
                    device.getDeviceType(),
                    device.getName()
            );
        } else if ("WARNING".equalsIgnoreCase(formattedStatus)) {
            alertService.createAlert(
                    "High Latency / Packet Loss: " + device.getName(),
                    "Device " + device.getName() + " reported high response times > 200ms or 15% packet drop.",
                    "WARNING",
                    "NETWORK",
                    device.getDeviceType(),
                    device.getName()
            );
        }

        auditLogService.log(
                authService.getCurrentUsername(),
                "DEVICE_STATUS_CHANGED",
                "NetworkDevice",
                saved.getId().toString(),
                clientIp,
                "SUCCESS",
                "Device " + saved.getName() + " status transitioned from " + oldStatus + " to " + formattedStatus
        );

        return mapToDto(saved);
    }

    @Transactional
    public void deleteDevice(Long id, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        NetworkDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        deviceRepository.delete(device);

        auditLogService.log(
                authService.getCurrentUsername(),
                "DEVICE_DELETED",
                "NetworkDevice",
                id.toString(),
                clientIp,
                "SUCCESS",
                "Deleted device " + device.getName() + " (" + device.getIpAddress() + ")"
        );
    }

    public DeviceDto mapToDto(NetworkDevice device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setDeviceType(device.getDeviceType());
        dto.setIpAddress(device.getIpAddress());
        dto.setMacAddress(device.getMacAddress());
        dto.setLocation(device.getLocation());
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setStatus(device.getStatus());
        dto.setLastSeen(device.getLastSeen());
        dto.setDescription(device.getDescription());

        if (device.getVlan() != null) {
            dto.setVlanId(device.getVlan().getId());
            dto.setVlanName(device.getVlan().getName());
            dto.setVlanNumber(device.getVlan().getVlanId());
        }

        List<NetworkInterface> interfaces = interfaceRepository.findByDeviceId(device.getId());
        List<DeviceDto.InterfaceDto> ifDtos = interfaces.stream()
                .map(i -> new DeviceDto.InterfaceDto(i.getId(), i.getInterfaceName(), i.getIpAddress(), i.getMacAddress(), i.getSpeed(), i.getStatus()))
                .collect(Collectors.toList());
        dto.setInterfaces(ifDtos);

        return dto;
    }
}
