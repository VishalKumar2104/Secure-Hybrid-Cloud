package com.sernms.service;

import com.sernms.dto.VlanDto;
import com.sernms.entity.Vlan;
import com.sernms.exception.BadRequestException;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.NetworkDeviceRepository;
import com.sernms.repository.VlanRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VlanService {

    private final VlanRepository vlanRepository;
    private final NetworkDeviceRepository deviceRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    public VlanService(VlanRepository vlanRepository, NetworkDeviceRepository deviceRepository,
                       AuditLogService auditLogService, AuthService authService) {
        this.vlanRepository = vlanRepository;
        this.deviceRepository = deviceRepository;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<VlanDto> getAllVlans() {
        return vlanRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VlanDto getVlanById(Long id) {
        Vlan vlan = vlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VLAN not found with id: " + id));
        return mapToDto(vlan);
    }

    @Transactional
    public VlanDto createVlan(VlanDto dto, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);

        if (vlanRepository.existsByVlanId(dto.getVlanId())) {
            throw new BadRequestException("VLAN with ID " + dto.getVlanId() + " already exists");
        }

        Vlan vlan = new Vlan(
                dto.getVlanId(),
                dto.getName(),
                dto.getSubnetCidr(),
                dto.getGatewayIp(),
                dto.getDescription()
        );

        Vlan saved = vlanRepository.save(vlan);

        auditLogService.log(
                authService.getCurrentUsername(),
                "VLAN_CREATED",
                "Vlan",
                saved.getId().toString(),
                clientIp,
                "SUCCESS",
                "Created VLAN " + saved.getVlanId() + " (" + saved.getName() + ")"
        );

        return mapToDto(saved);
    }

    @Transactional
    public VlanDto updateVlan(Long id, VlanDto dto, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        Vlan vlan = vlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VLAN not found with id: " + id));

        vlan.setName(dto.getName());
        vlan.setSubnetCidr(dto.getSubnetCidr());
        vlan.setGatewayIp(dto.getGatewayIp());
        vlan.setDescription(dto.getDescription());

        Vlan updated = vlanRepository.save(vlan);

        auditLogService.log(
                authService.getCurrentUsername(),
                "VLAN_UPDATED",
                "Vlan",
                updated.getId().toString(),
                clientIp,
                "SUCCESS",
                "Updated VLAN " + updated.getVlanId()
        );

        return mapToDto(updated);
    }

    @Transactional
    public void deleteVlan(Long id, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        Vlan vlan = vlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VLAN not found with id: " + id));

        long deviceCount = deviceRepository.findByVlanId(id).size();
        if (deviceCount > 0) {
            throw new BadRequestException("Cannot delete VLAN: " + deviceCount + " devices are assigned to this VLAN");
        }

        vlanRepository.delete(vlan);

        auditLogService.log(
                authService.getCurrentUsername(),
                "VLAN_DELETED",
                "Vlan",
                id.toString(),
                clientIp,
                "SUCCESS",
                "Deleted VLAN " + vlan.getVlanId() + " (" + vlan.getName() + ")"
        );
    }

    private VlanDto mapToDto(Vlan vlan) {
        long count = deviceRepository.findByVlanId(vlan.getId()).size();
        return new VlanDto(
                vlan.getId(),
                vlan.getVlanId(),
                vlan.getName(),
                vlan.getSubnetCidr(),
                vlan.getGatewayIp(),
                vlan.getDescription(),
                count
        );
    }
}
