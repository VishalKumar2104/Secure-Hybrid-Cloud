package com.sernms.controller;

import com.sernms.dto.DashboardStatsDto;
import com.sernms.dto.TelemetryPointDto;
import com.sernms.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Central Monitoring", description = "Endpoints for aggregate dashboard metrics, system health, and time-series telemetry")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Retrieve high-level dashboard metrics, entity counts, health percentage, and recent alerts")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(monitoringService.getDashboardStats());
    }

    @GetMapping("/telemetry")
    @Operation(summary = "Retrieve overall multi-point telemetry series for real-time Chart.js graphs")
    public ResponseEntity<List<TelemetryPointDto>> getTelemetrySeries() {
        return ResponseEntity.ok(monitoringService.getTelemetrySeries());
    }
}
