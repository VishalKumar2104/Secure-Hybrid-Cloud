package com.sernms.controller;

import com.sernms.dto.DemoSimulationRequest;
import com.sernms.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Controlled Failure & Demo Lab", description = "Endpoints for academic evaluation: inject failures, toggle Router-01 offline, spike EC2 CPU, and trigger real-time alert cascades")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/simulate")
    @Operation(summary = "Execute controlled failure injection scenario (DEVICE_FAIL, DEVICE_RESTORE, HIGH_CPU, NORMAL_CPU, VPN_FAIL, VPN_RESTORE)")
    public ResponseEntity<Map<String, Object>> simulateScenario(@RequestBody DemoSimulationRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(demoService.executeScenario(request, servletRequest));
    }
}
