package com.sernms.controller;

import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.TelemetryPointDto;
import com.sernms.service.AwsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aws")
@Tag(name = "AWS Cloud Management", description = "Endpoints for managing EC2 instances, RDS databases, S3 buckets, and IPSec VPN Gateway")
@PreAuthorize("hasAnyRole('ADMIN', 'CLOUD_ADMIN')")
public class AwsController {

    private final AwsService awsService;

    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    @GetMapping("/resources")
    @Operation(summary = "List all cloud infrastructure resources")
    public ResponseEntity<List<AwsResourceDto>> getAllResources() {
        return ResponseEntity.ok(awsService.getAllResources());
    }

    @GetMapping("/resources/type/{type}")
    @Operation(summary = "Filter AWS resources by type (EC2, RDS, S3, ALB, VGW)")
    public ResponseEntity<List<AwsResourceDto>> getResourcesByType(@PathVariable String type) {
        return ResponseEntity.ok(awsService.getResourcesByType(type));
    }

    @GetMapping("/resources/{id}")
    @Operation(summary = "Get AWS resource details by Resource ID")
    public ResponseEntity<AwsResourceDto> getResourceById(@PathVariable String id) {
        return ResponseEntity.ok(awsService.getResourceById(id));
    }

    @PostMapping("/resources/{id}/start")
    @Operation(summary = "Start an EC2 instance")
    public ResponseEntity<String> startInstance(@PathVariable String id, HttpServletRequest servletRequest) {
        awsService.startInstance(id, servletRequest);
        return ResponseEntity.ok("Start instance request initiated successfully for " + id);
    }

    @PostMapping("/resources/{id}/stop")
    @Operation(summary = "Stop an EC2 instance")
    public ResponseEntity<String> stopInstance(@PathVariable String id, HttpServletRequest servletRequest) {
        awsService.stopInstance(id, servletRequest);
        return ResponseEntity.ok("Stop instance request initiated successfully for " + id);
    }

    @GetMapping("/resources/{id}/metrics")
    @Operation(summary = "Fetch CloudWatch time-series metrics (CPU, RAM, Network I/O) for a cloud resource")
    public ResponseEntity<List<TelemetryPointDto>> getResourceMetrics(@PathVariable String id) {
        return ResponseEntity.ok(awsService.getResourceMetrics(id));
    }

    @GetMapping("/vpn/status")
    @Operation(summary = "Query AWS Site-to-Site IPSec VPN tunnel connection state")
    public ResponseEntity<Map<String, String>> getVpnStatus() {
        return ResponseEntity.ok(Map.of("vpnStatus", awsService.getVpnStatus()));
    }

    @PutMapping("/vpn/status")
    @Operation(summary = "Update IPSec VPN tunnel state (CONNECTED, DISCONNECTED)")
    public ResponseEntity<Map<String, String>> setVpnStatus(@RequestBody Map<String, String> body, HttpServletRequest servletRequest) {
        String status = body.getOrDefault("status", "CONNECTED");
        awsService.setVpnStatus(status, servletRequest);
        return ResponseEntity.ok(Map.of("vpnStatus", status));
    }
}
