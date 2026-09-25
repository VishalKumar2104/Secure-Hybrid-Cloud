package com.sernms.controller;

import com.sernms.dto.DeviceDto;
import com.sernms.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/network/devices")
@Tag(name = "Network Device Management", description = "Endpoints for enterprise routers, switches, firewalls, and servers")
@PreAuthorize("hasAnyRole('ADMIN', 'NETWORK_ADMIN')")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "List all network devices")
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get network device details by ID")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search devices by name, IP, location, or manufacturer")
    public ResponseEntity<List<DeviceDto>> searchDevices(@RequestParam String query) {
        return ResponseEntity.ok(deviceService.searchDevices(query));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter devices by status (ONLINE, OFFLINE, WARNING, MAINTENANCE)")
    public ResponseEntity<List<DeviceDto>> getDevicesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deviceService.getDevicesByStatus(status));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filter devices by type (ROUTER, SWITCH, FIREWALL, SERVER)")
    public ResponseEntity<List<DeviceDto>> getDevicesByType(@PathVariable String type) {
        return ResponseEntity.ok(deviceService.getDevicesByType(type));
    }

    @PostMapping
    @Operation(summary = "Add a new network device to inventory")
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto dto, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(deviceService.createDevice(dto, servletRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update network device specifications")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceDto dto, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(deviceService.updateDevice(id, dto, servletRequest));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update device operational status (ONLINE, OFFLINE, WARNING, MAINTENANCE)")
    public ResponseEntity<DeviceDto> updateDeviceStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap, HttpServletRequest servletRequest) {
        String newStatus = statusMap.getOrDefault("status", "ONLINE");
        return ResponseEntity.ok(deviceService.updateDeviceStatus(id, newStatus, servletRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Decommission and remove a network device")
    public ResponseEntity<String> deleteDevice(@PathVariable Long id, HttpServletRequest servletRequest) {
        deviceService.deleteDevice(id, servletRequest);
        return ResponseEntity.ok("Device deleted successfully");
    }
}
