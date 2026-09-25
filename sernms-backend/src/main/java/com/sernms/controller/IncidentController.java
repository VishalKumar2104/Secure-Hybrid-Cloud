package com.sernms.controller;

import com.sernms.dto.IncidentCommentRequest;
import com.sernms.dto.IncidentRequest;
import com.sernms.dto.IncidentResponse;
import com.sernms.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incident Management", description = "Endpoints for ticket reporting, assignment, status workflow, and resolution logs")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "List all incidents")
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident details including comment history")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter incidents by status (OPEN, ASSIGNED, INVESTIGATING, RESOLVED, CLOSED)")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(incidentService.getIncidentsByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Report a new incident ticket")
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(incidentService.createIncident(request, servletRequest));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Transition ticket status (e.g. ASSIGNED, INVESTIGATING, RESOLVED, CLOSED)")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest servletRequest) {
        String status = body.getOrDefault("status", "OPEN");
        return ResponseEntity.ok(incidentService.updateIncidentStatus(id, status, servletRequest));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign ticket to an engineer")
    public ResponseEntity<IncidentResponse> assignIncident(@PathVariable Long id, @RequestBody Map<String, Long> body, HttpServletRequest servletRequest) {
        Long assigneeId = body.get("assigneeId");
        return ResponseEntity.ok(incidentService.assignIncident(id, assigneeId, servletRequest));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add an investigation comment to an incident")
    public ResponseEntity<IncidentResponse.CommentDto> addComment(@PathVariable Long id, @Valid @RequestBody IncidentCommentRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(incidentService.addComment(id, request, servletRequest));
    }
}
