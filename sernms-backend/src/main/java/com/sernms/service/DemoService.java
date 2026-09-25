package com.sernms.service;

import com.sernms.dto.DemoSimulationRequest;
import com.sernms.dto.DeviceDto;
import com.sernms.entity.NetworkDevice;
import com.sernms.repository.NetworkDeviceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DemoService {

    private final DeviceService deviceService;
    private final NetworkDeviceRepository deviceRepository;
    private final AwsService awsService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    public DemoService(DeviceService deviceService,
                       NetworkDeviceRepository deviceRepository,
                       AwsService awsService,
                       AlertService alertService,
                       AuditLogService auditLogService,
                       AuthService authService) {
        this.deviceService = deviceService;
        this.deviceRepository = deviceRepository;
        this.awsService = awsService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    @Transactional
    public Map<String, Object> executeScenario(DemoSimulationRequest request, HttpServletRequest servletRequest) {
        Map<String, Object> result = new HashMap<>();
        String clientIp = auditLogService.extractClientIp(servletRequest);
        String scenario = request.getScenario() != null ? request.getScenario().toUpperCase() : "CUSTOM";

        switch (scenario) {
            case "DEVICE_FAIL": {
                String target = request.getTargetId() != null ? request.getTargetId() : "R-EDGE-01";
                NetworkDevice device = deviceRepository.findByName(target)
                        .orElse(deviceRepository.findAll().stream().findFirst().orElse(null));

                if (device != null) {
                    deviceService.updateDeviceStatus(device.getId(), "OFFLINE", servletRequest);
                    result.put("status", "SUCCESS");
                    result.put("message", "Simulated failure: Device " + device.getName() + " is now OFFLINE. Critical alert triggered.");
                    result.put("deviceId", device.getId());
                    result.put("deviceName", device.getName());
                } else {
                    result.put("status", "ERROR");
                    result.put("message", "Target device not found");
                }
                break;
            }
            case "DEVICE_RESTORE": {
                String target = request.getTargetId() != null ? request.getTargetId() : "R-EDGE-01";
                NetworkDevice device = deviceRepository.findByName(target)
                        .orElse(deviceRepository.findAll().stream().findFirst().orElse(null));

                if (device != null) {
                    deviceService.updateDeviceStatus(device.getId(), "ONLINE", servletRequest);
                    result.put("status", "SUCCESS");
                    result.put("message", "Restored device " + device.getName() + " to ONLINE. Health metrics normalized.");
                    result.put("deviceId", device.getId());
                    result.put("deviceName", device.getName());
                } else {
                    result.put("status", "ERROR");
                    result.put("message", "Target device not found");
                }
                break;
            }
            case "HIGH_CPU": {
                String target = request.getTargetId() != null ? request.getTargetId() : "i-07f9c81a2b";
                double cpu = request.getCpuValue() != null ? request.getCpuValue() : 94.8;
                awsService.simulateCpuSpike(target, cpu, servletRequest);
                result.put("status", "SUCCESS");
                result.put("message", "CloudWatch Metric CPU spiked to " + cpu + "% on " + target + ". Critical alert triggered.");
                result.put("targetId", target);
                result.put("cpuValue", cpu);
                break;
            }
            case "NORMAL_CPU": {
                String target = request.getTargetId() != null ? request.getTargetId() : "i-07f9c81a2b";
                awsService.simulateCpuSpike(target, 28.5, servletRequest);
                result.put("status", "SUCCESS");
                result.put("message", "CPU utilization on " + target + " normalized to 28.5%.");
                result.put("targetId", target);
                break;
            }
            case "VPN_FAIL": {
                awsService.setVpnStatus("DISCONNECTED", servletRequest);
                result.put("status", "SUCCESS");
                result.put("message", "IPSec Site-to-Site VPN tunnel disconnected. Hybrid cloud connectivity severed.");
                break;
            }
            case "VPN_RESTORE": {
                awsService.setVpnStatus("CONNECTED", servletRequest);
                result.put("status", "SUCCESS");
                result.put("message", "IPSec Site-to-Site VPN tunnel re-established with AWS VGW.");
                break;
            }
            default:
                result.put("status", "ERROR");
                result.put("message", "Unknown scenario: " + scenario);
        }

        auditLogService.log(
                authService.getCurrentUsername(),
                "DEMO_SCENARIO_EXECUTED",
                "SimulationLab",
                scenario,
                clientIp,
                "SUCCESS",
                "Executed demo scenario: " + scenario + " -> " + result.get("message")
        );

        return result;
    }
}
