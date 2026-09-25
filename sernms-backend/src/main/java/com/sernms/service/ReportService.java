package com.sernms.service;

import com.sernms.dto.AuditLogDto;
import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.DeviceDto;
import com.sernms.dto.IncidentResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final DeviceService deviceService;
    private final AwsService awsService;
    private final IncidentService incidentService;
    private final AuditLogService auditLogService;
    private final MonitoringService monitoringService;

    public ReportService(DeviceService deviceService, AwsService awsService,
                         IncidentService incidentService, AuditLogService auditLogService,
                         MonitoringService monitoringService) {
        this.deviceService = deviceService;
        this.awsService = awsService;
        this.incidentService = incidentService;
        this.auditLogService = auditLogService;
        this.monitoringService = monitoringService;
    }

    public Map<String, Object> getSystemHealthReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("dashboardStats", monitoringService.getDashboardStats());
        report.put("networkDevices", deviceService.getAllDevices());
        report.put("awsResources", awsService.getAllResources());
        report.put("recentIncidents", incidentService.getAllIncidents());
        report.put("recentAuditLogs", auditLogService.getAllLogs().stream().limit(25).toList());
        return report;
    }

    public String exportDevicesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Device ID,Device Name,Type,IP Address,MAC Address,Location,Manufacturer,Status,VLAN\n");

        for (DeviceDto d : deviceService.getAllDevices()) {
            csv.append(d.getId()).append(",")
               .append(escape(d.getName())).append(",")
               .append(d.getDeviceType()).append(",")
               .append(d.getIpAddress()).append(",")
               .append(d.getMacAddress() != null ? d.getMacAddress() : "").append(",")
               .append(escape(d.getLocation())).append(",")
               .append(escape(d.getManufacturer())).append(",")
               .append(d.getStatus()).append(",")
               .append(d.getVlanName() != null ? escape(d.getVlanName()) : "N/A")
               .append("\n");
        }
        return csv.toString();
    }

    public String exportAwsResourcesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Resource ID,Name,Type,Region,Private IP,Public IP,Status,Instance Type\n");

        for (AwsResourceDto r : awsService.getAllResources()) {
            csv.append(r.getResourceId()).append(",")
               .append(escape(r.getResourceName())).append(",")
               .append(r.getResourceType()).append(",")
               .append(r.getRegion()).append(",")
               .append(r.getPrivateIp() != null ? r.getPrivateIp() : "N/A").append(",")
               .append(r.getPublicIp() != null ? r.getPublicIp() : "N/A").append(",")
               .append(r.getStatus()).append(",")
               .append(escape(r.getInstanceType()))
               .append("\n");
        }
        return csv.toString();
    }

    public String exportIncidentsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Ticket Number,Title,Category,Priority,Status,Reporter,Assignee,Created At,Resolved At\n");

        for (IncidentResponse i : incidentService.getAllIncidents()) {
            csv.append(i.getTicketNumber()).append(",")
               .append(escape(i.getTitle())).append(",")
               .append(i.getCategory()).append(",")
               .append(i.getPriority()).append(",")
               .append(i.getStatus()).append(",")
               .append(escape(i.getReporterName())).append(",")
               .append(i.getAssigneeName() != null ? escape(i.getAssigneeName()) : "Unassigned").append(",")
               .append(i.getCreatedAt()).append(",")
               .append(i.getResolvedAt() != null ? i.getResolvedAt() : "N/A")
               .append("\n");
        }
        return csv.toString();
    }

    public String exportAuditLogsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Username,Action,Entity,Entity ID,IP Address,Status,Details\n");

        for (AuditLogDto a : auditLogService.getAllLogs()) {
            csv.append(a.getTimestamp()).append(",")
               .append(escape(a.getUsername())).append(",")
               .append(a.getAction()).append(",")
               .append(a.getEntityName() != null ? a.getEntityName() : "").append(",")
               .append(a.getEntityId() != null ? a.getEntityId() : "").append(",")
               .append(a.getIpAddress() != null ? a.getIpAddress() : "").append(",")
               .append(a.getStatus()).append(",")
               .append(escape(a.getDetails()))
               .append("\n");
        }
        return csv.toString();
    }

    private String escape(String val) {
        if (val == null) return "";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
