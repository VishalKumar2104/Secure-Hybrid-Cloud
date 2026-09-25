package com.sernms.controller;

import com.sernms.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Analytics", description = "Endpoints for executive system health summaries and CSV exports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get full system health, inventory, and incident summary report")
    public ResponseEntity<Map<String, Object>> getSummaryReport() {
        return ResponseEntity.ok(reportService.getSystemHealthReport());
    }

    @GetMapping("/export/{type}")
    @Operation(summary = "Export data as CSV (types: devices, aws, incidents, audit)")
    public ResponseEntity<String> exportCsv(@PathVariable String type) {
        String csv;
        String filename;

        switch (type.toLowerCase()) {
            case "devices":
                csv = reportService.exportDevicesCsv();
                filename = "sernms-network-devices.csv";
                break;
            case "aws":
                csv = reportService.exportAwsResourcesCsv();
                filename = "sernms-aws-resources.csv";
                break;
            case "incidents":
                csv = reportService.exportIncidentsCsv();
                filename = "sernms-incidents.csv";
                break;
            case "audit":
                csv = reportService.exportAuditLogsCsv();
                filename = "sernms-audit-logs.csv";
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid export type. Allowed: devices, aws, incidents, audit");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
