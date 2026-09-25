package com.sernms.dto;

import java.util.List;

public class DashboardStatsDto {
    private long totalUsers;
    private long activeUsers;

    private long totalNetworkDevices;
    private long onlineNetworkDevices;
    private long offlineNetworkDevices;
    private long warningNetworkDevices;

    private long totalAwsResources;
    private long runningAwsResources;
    private long stoppedAwsResources;

    private long healthyResources;
    private long warningResources;
    private long criticalResources;

    private long openIncidents;
    private long investigatingIncidents;
    private long resolvedIncidents;

    private long criticalAlerts;
    private long unacknowledgedAlerts;

    private String hybridVpnStatus; // CONNECTED, DEGRADED, DISCONNECTED
    private double systemHealthPercentage; // e.g. 96.5%

    private List<AlertDto> recentAlerts;
    private List<IncidentResponse> recentIncidents;
    private List<AuditLogDto> recentActivities;

    public DashboardStatsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getTotalNetworkDevices() { return totalNetworkDevices; }
    public void setTotalNetworkDevices(long totalNetworkDevices) { this.totalNetworkDevices = totalNetworkDevices; }

    public long getOnlineNetworkDevices() { return onlineNetworkDevices; }
    public void setOnlineNetworkDevices(long onlineNetworkDevices) { this.onlineNetworkDevices = onlineNetworkDevices; }

    public long getOfflineNetworkDevices() { return offlineNetworkDevices; }
    public void setOfflineNetworkDevices(long offlineNetworkDevices) { this.offlineNetworkDevices = offlineNetworkDevices; }

    public long getWarningNetworkDevices() { return warningNetworkDevices; }
    public void setWarningNetworkDevices(long warningNetworkDevices) { this.warningNetworkDevices = warningNetworkDevices; }

    public long getTotalAwsResources() { return totalAwsResources; }
    public void setTotalAwsResources(long totalAwsResources) { this.totalAwsResources = totalAwsResources; }

    public long getRunningAwsResources() { return runningAwsResources; }
    public void setRunningAwsResources(long runningAwsResources) { this.runningAwsResources = runningAwsResources; }

    public long getStoppedAwsResources() { return stoppedAwsResources; }
    public void setStoppedAwsResources(long stoppedAwsResources) { this.stoppedAwsResources = stoppedAwsResources; }

    public long getHealthyResources() { return healthyResources; }
    public void setHealthyResources(long healthyResources) { this.healthyResources = healthyResources; }

    public long getWarningResources() { return warningResources; }
    public void setWarningResources(long warningResources) { this.warningResources = warningResources; }

    public long getCriticalResources() { return criticalResources; }
    public void setCriticalResources(long criticalResources) { this.criticalResources = criticalResources; }

    public long getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(long openIncidents) { this.openIncidents = openIncidents; }

    public long getInvestigatingIncidents() { return investigatingIncidents; }
    public void setInvestigatingIncidents(long investigatingIncidents) { this.investigatingIncidents = investigatingIncidents; }

    public long getResolvedIncidents() { return resolvedIncidents; }
    public void setResolvedIncidents(long resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }

    public long getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(long criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public long getUnacknowledgedAlerts() { return unacknowledgedAlerts; }
    public void setUnacknowledgedAlerts(long unacknowledgedAlerts) { this.unacknowledgedAlerts = unacknowledgedAlerts; }

    public String getHybridVpnStatus() { return hybridVpnStatus; }
    public void setHybridVpnStatus(String hybridVpnStatus) { this.hybridVpnStatus = hybridVpnStatus; }

    public double getSystemHealthPercentage() { return systemHealthPercentage; }
    public void setSystemHealthPercentage(double systemHealthPercentage) { this.systemHealthPercentage = systemHealthPercentage; }

    public List<AlertDto> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<AlertDto> recentAlerts) { this.recentAlerts = recentAlerts; }

    public List<IncidentResponse> getRecentIncidents() { return recentIncidents; }
    public void setRecentIncidents(List<IncidentResponse> recentIncidents) { this.recentIncidents = recentIncidents; }

    public List<AuditLogDto> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<AuditLogDto> recentActivities) { this.recentActivities = recentActivities; }
}
