package com.sernms.service;

import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.TelemetryPointDto;
import com.sernms.service.provider.AwsProvider;
import com.sernms.service.provider.LiveAwsProvider;
import com.sernms.service.provider.SimulatedAwsProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsService {

    private final LiveAwsProvider liveProvider;
    private final SimulatedAwsProvider simulatedProvider;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final AuthService authService;
    private final String activeMode;

    public AwsService(
            LiveAwsProvider liveProvider,
            SimulatedAwsProvider simulatedProvider,
            AlertService alertService,
            AuditLogService auditLogService,
            AuthService authService,
            @Value("${app.aws.mode:simulated}") String activeMode) {
        this.liveProvider = liveProvider;
        this.simulatedProvider = simulatedProvider;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
        this.authService = authService;
        this.activeMode = activeMode;
    }

    private AwsProvider getProvider() {
        return "live".equalsIgnoreCase(activeMode) ? liveProvider : simulatedProvider;
    }

    public List<AwsResourceDto> getAllResources() {
        return getProvider().getResources();
    }

    public List<AwsResourceDto> getResourcesByType(String type) {
        return getProvider().getResources().stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public AwsResourceDto getResourceById(String resourceId) {
        return getProvider().getResource(resourceId);
    }

    public void startInstance(String resourceId, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        getProvider().startInstance(resourceId);

        auditLogService.log(
                authService.getCurrentUsername(),
                "AWS_INSTANCE_START",
                "AwsResource",
                resourceId,
                clientIp,
                "SUCCESS",
                "Sent start command for AWS resource " + resourceId
        );
    }

    public void stopInstance(String resourceId, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        getProvider().stopInstance(resourceId);

        auditLogService.log(
                authService.getCurrentUsername(),
                "AWS_INSTANCE_STOP",
                "AwsResource",
                resourceId,
                clientIp,
                "SUCCESS",
                "Sent stop command for AWS resource " + resourceId
        );
    }

    public List<TelemetryPointDto> getResourceMetrics(String resourceId) {
        return getProvider().getResourceMetrics(resourceId);
    }

    public String getVpnStatus() {
        return getProvider().getVpnStatus();
    }

    public void setVpnStatus(String status, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        getProvider().setVpnStatus(status);

        if ("DISCONNECTED".equalsIgnoreCase(status) || "DOWN".equalsIgnoreCase(status)) {
            alertService.createAlert(
                    "Hybrid Cloud IPSec VPN Tunnel Down",
                    "Site-to-Site IPSec VPN tunnel to AWS Virtual Private Gateway is DISCONNECTED. Routing traffic halted.",
                    "CRITICAL",
                    "SECURITY",
                    "VPN",
                    "vgw-sernms-hybrid-01"
            );
        }

        auditLogService.log(
                authService.getCurrentUsername(),
                "HYBRID_VPN_STATUS_CHANGED",
                "VPN",
                "vgw-sernms-hybrid-01",
                clientIp,
                "SUCCESS",
                "IPSec VPN tunnel status transitioned to " + status
        );
    }

    public void simulateCpuSpike(String resourceId, double cpuPercentage, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        getProvider().simulateCpuSpike(resourceId, cpuPercentage);

        if (cpuPercentage >= 90.0) {
            alertService.createAlert(
                    "AWS EC2 Critical CPU Spike: " + resourceId,
                    "CloudWatch alarm triggered: Instance " + resourceId + " CPU utilization reached " + cpuPercentage + "% (> 90% threshold).",
                    "CRITICAL",
                    "CLOUD",
                    "EC2",
                    resourceId
            );
        } else if (cpuPercentage >= 80.0) {
            alertService.createAlert(
                    "AWS EC2 High CPU Warning: " + resourceId,
                    "CloudWatch alarm triggered: Instance " + resourceId + " CPU utilization reached " + cpuPercentage + "% (> 80% threshold).",
                    "WARNING",
                    "CLOUD",
                    "EC2",
                    resourceId
            );
        }

        auditLogService.log(
                authService.getCurrentUsername(),
                "AWS_SIMULATE_CPU_SPIKE",
                "AwsResource",
                resourceId,
                clientIp,
                "SUCCESS",
                "Simulated CPU spike to " + cpuPercentage + "% on " + resourceId
        );
    }
}
