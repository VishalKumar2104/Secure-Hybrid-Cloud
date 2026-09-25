package com.sernms.controller;

import com.sernms.dto.VlanDto;
import com.sernms.service.VlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/vlans")
@Tag(name = "VLAN Management", description = "Endpoints for managing campus VLAN subnets and gateway assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'NETWORK_ADMIN')")
public class VlanController {

    private final VlanService vlanService;

    public VlanController(VlanService vlanService) {
        this.vlanService = vlanService;
    }

    @GetMapping
    @Operation(summary = "List all configured VLAN subnets")
    public ResponseEntity<List<VlanDto>> getAllVlans() {
        return ResponseEntity.ok(vlanService.getAllVlans());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get VLAN details by ID")
    public ResponseEntity<VlanDto> getVlanById(@PathVariable Long id) {
        return ResponseEntity.ok(vlanService.getVlanById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new VLAN subnet")
    public ResponseEntity<VlanDto> createVlan(@Valid @RequestBody VlanDto dto, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vlanService.createVlan(dto, servletRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update VLAN parameters")
    public ResponseEntity<VlanDto> updateVlan(@PathVariable Long id, @Valid @RequestBody VlanDto dto, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vlanService.updateVlan(id, dto, servletRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete VLAN subnet")
    public ResponseEntity<String> deleteVlan(@PathVariable Long id, HttpServletRequest servletRequest) {
        vlanService.deleteVlan(id, servletRequest);
        return ResponseEntity.ok("VLAN deleted successfully");
    }
}
