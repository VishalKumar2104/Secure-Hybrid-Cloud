package com.sernms.service;

import com.sernms.dto.DashboardStatsDto;
import com.sernms.dto.TelemetryPointDto;
import com.sernms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoringService {

    private final UserRepository userRepository;
    private final NetworkDeviceRepository deviceRepository;
    private final AwsService awsService;
    private final IncidentService incidentService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;

    public MonitoringService(UserRepository userRepository,
                             NetworkDeviceRepository deviceRepository,
                             AwsService awsService,
                             IncidentService incidentService,
                             AlertService alertService,
                             AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.awsService = awsService;
        this.incidentService = incidentService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. User stats
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByActive(true));

        // 2. Network device stats
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.countByStatus("ONLINE");
        long offlineDevices = deviceRepository.countByStatus("OFFLINE");
        long warningDevices = deviceRepository.countByStatus("WARNING");
        stats.setTotalNetworkDevices(totalDevices);
        stats.setOnlineNetworkDevices(onlineDevices);
        stats.setOfflineNetworkDevices(offlineDevices);
        stats.setWarningNetworkDevices(warningDevices);

        // 3. AWS Resource stats
        var awsResources = awsService.getAllResources();
        long totalAws = awsResources.size();
        long runningAws = awsResources.stream().filter(r -> "RUNNING".equalsIgnoreCase(r.getStatus()) || "AVAILABLE".equalsIgnoreCase(r.getStatus()) || "HEALTHY".equalsIgnoreCase(r.getStatus())).count();
        long stoppedAws = awsResources.stream().filter(r -> "STOPPED".equalsIgnoreCase(r.getStatus())).count();
        stats.setTotalAwsResources(totalAws);
        stats.setRunningAwsResources(runningAws);
        stats.setStoppedAwsResources(stoppedAws);

        // 4. Incident stats
        var allIncidents = incidentService.getAllIncidents();
        long openIncidents = allIncidents.stream().filter(i -> "OPEN".equalsIgnoreCase(i.getStatus()) || "ASSIGNED".equalsIgnoreCase(i.getStatus())).count();
        long investigatingIncidents = allIncidents.stream().filter(i -> "INVESTIGATING".equalsIgnoreCase(i.getStatus())).count();
        long resolvedIncidents = allIncidents.stream().filter(i -> "RESOLVED".equalsIgnoreCase(i.getStatus()) || "CLOSED".equalsIgnoreCase(i.getStatus())).count();
        stats.setOpenIncidents(openIncidents);
        stats.setInvestigatingIncidents(investigatingIncidents);
        stats.setResolvedIncidents(resolvedIncidents);

        // 5. Alert stats
        var activeAlerts = alertService.getActiveAlerts();
        long criticalAlerts = activeAlerts.stream().filter(a -> "CRITICAL".equalsIgnoreCase(a.getSeverity())).count();
        stats.setCriticalAlerts(criticalAlerts);
        stats.setUnacknowledgedAlerts(activeAlerts.size());

        // 6. VPN & System Health
        stats.setHybridVpnStatus(awsService.getVpnStatus());

        long totalEntities = totalDevices + totalAws;
        long healthyEntities = onlineDevices + runningAws;
        double health = totalEntities > 0 ? (double) healthyEntities / totalEntities * 100.0 : 100.0;
        if (criticalAlerts > 0) health = Math.max(50.0, health - (criticalAlerts * 8.0));
        stats.setSystemHealthPercentage(Math.round(health * 10.0) / 10.0);

        stats.setHealthyResources(healthyEntities);
        stats.setWarningResources(warningDevices);
        stats.setCriticalResources(offlineDevices + criticalAlerts);

        // 7. Recent feeds
        stats.setRecentAlerts(activeAlerts.stream().limit(5).toList());
        stats.setRecentIncidents(allIncidents.stream().limit(5).toList());
        stats.setRecentActivities(auditLogService.getAllLogs().stream().limit(6).toList());

        return stats;
    }

    public List<TelemetryPointDto> getTelemetrySeries() {
        List<TelemetryPointDto> points = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        for (int i = 11; i >= 0; i--) {
            LocalDateTime t = now.minusMinutes(i * 5);
            double cpu = 28.0 + (Math.sin(i * 0.8) * 12.0) + (Math.random() * 5.0);
            double mem = 52.0 + (Math.random() * 3.5);
            double netIn = 45.0 + (Math.cos(i * 0.6) * 20.0) + (Math.random() * 10.0);
            double netOut = 38.0 + (Math.sin(i * 0.5) * 15.0) + (Math.random() * 8.0);

            points.add(new TelemetryPointDto(
                    t.format(timeFmt),
                    Math.round(cpu * 10.0) / 10.0,
                    Math.round(mem * 10.0) / 10.0,
                    Math.round(netIn * 10.0) / 10.0,
                    Math.round(netOut * 10.0) / 10.0
            ));
        }
        return points;
    }
}
