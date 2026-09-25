package com.sernms.service;

import com.sernms.dto.IncidentCommentRequest;
import com.sernms.dto.IncidentRequest;
import com.sernms.dto.IncidentResponse;
import com.sernms.entity.Incident;
import com.sernms.entity.IncidentComment;
import com.sernms.entity.User;
import com.sernms.exception.BadRequestException;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.IncidentCommentRepository;
import com.sernms.repository.IncidentRepository;
import com.sernms.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentCommentRepository commentRepository,
                           UserRepository userRepository,
                           AuthService authService,
                           AuditLogService auditLogService,
                           NotificationService notificationService) {
        this.incidentRepository = incidentRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findRecentIncidents().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
        return mapToResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getIncidentsByStatus(String status) {
        return incidentRepository.findByStatus(status.toUpperCase()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getIncidentsByReporter(Long reporterId) {
        return incidentRepository.findByReporterId(reporterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        User reporter = authService.getCurrentUser();

        long count = incidentRepository.count() + 1;
        String ticketNumber = "INC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + String.format("%03d", count);

        Incident incident = new Incident(
                ticketNumber,
                request.getTitle(),
                request.getDescription(),
                request.getCategory().toUpperCase(),
                request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM",
                reporter,
                request.getImpactedResourceType(),
                request.getImpactedResourceId()
        );

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new BadRequestException("Assignee user not found"));
            incident.setAssignee(assignee);
            incident.setStatus("ASSIGNED");
        }

        Incident saved = incidentRepository.save(incident);

        notificationService.createGlobalNotification(
                "Incident Created: " + saved.getTicketNumber(),
                saved.getTitle() + " [Priority: " + saved.getPriority() + "]",
                saved.getPriority().equalsIgnoreCase("CRITICAL") ? "ALERT" : "INFO"
        );

        auditLogService.log(
                reporter.getUsername(),
                "INCIDENT_CREATED",
                "Incident",
                saved.getId().toString(),
                clientIp,
                "SUCCESS",
                "Created incident " + saved.getTicketNumber() + ": " + saved.getTitle()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(Long id, String newStatus, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        String oldStatus = incident.getStatus();
        String formattedStatus = newStatus.toUpperCase();
        incident.setStatus(formattedStatus);

        if ("RESOLVED".equalsIgnoreCase(formattedStatus) || "CLOSED".equalsIgnoreCase(formattedStatus)) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        Incident updated = incidentRepository.save(incident);

        auditLogService.log(
                authService.getCurrentUsername(),
                "INCIDENT_STATUS_CHANGED",
                "Incident",
                updated.getId().toString(),
                clientIp,
                "SUCCESS",
                "Incident " + updated.getTicketNumber() + " transitioned from " + oldStatus + " to " + formattedStatus
        );

        return mapToResponse(updated);
    }

    @Transactional
    public IncidentResponse assignIncident(Long id, Long assigneeId, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));

        incident.setAssignee(assignee);
        if ("OPEN".equalsIgnoreCase(incident.getStatus())) {
            incident.setStatus("ASSIGNED");
        }

        Incident updated = incidentRepository.save(incident);

        notificationService.createUserNotification(
                assignee,
                "Incident Assigned: " + incident.getTicketNumber(),
                "You have been assigned to investigate: " + incident.getTitle(),
                "INFO"
        );

        auditLogService.log(
                authService.getCurrentUsername(),
                "INCIDENT_ASSIGNED",
                "Incident",
                updated.getId().toString(),
                clientIp,
                "SUCCESS",
                "Assigned " + incident.getTicketNumber() + " to " + assignee.getUsername()
        );

        return mapToResponse(updated);
    }

    @Transactional
    public IncidentResponse.CommentDto addComment(Long id, IncidentCommentRequest request, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        User currentUser = authService.getCurrentUser();

        IncidentComment comment = new IncidentComment(incident, currentUser, request.getCommentText());
        IncidentComment saved = commentRepository.save(comment);

        auditLogService.log(
                currentUser.getUsername(),
                "INCIDENT_COMMENT_ADDED",
                "Incident",
                incident.getId().toString(),
                clientIp,
                "SUCCESS",
                "Posted update on incident " + incident.getTicketNumber()
        );

        return new IncidentResponse.CommentDto(
                saved.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                saved.getCommentText(),
                saved.getCreatedAt()
        );
    }

    public IncidentResponse mapToResponse(Incident incident) {
        IncidentResponse res = new IncidentResponse();
        res.setId(incident.getId());
        res.setTicketNumber(incident.getTicketNumber());
        res.setTitle(incident.getTitle());
        res.setDescription(incident.getDescription());
        res.setCategory(incident.getCategory());
        res.setPriority(incident.getPriority());
        res.setStatus(incident.getStatus());
        res.setReporterId(incident.getReporter().getId());
        res.setReporterName(incident.getReporter().getUsername());

        if (incident.getAssignee() != null) {
            res.setAssigneeId(incident.getAssignee().getId());
            res.setAssigneeName(incident.getAssignee().getUsername());
        }

        res.setImpactedResourceType(incident.getImpactedResourceType());
        res.setImpactedResourceId(incident.getImpactedResourceId());
        res.setCreatedAt(incident.getCreatedAt());
        res.setUpdatedAt(incident.getUpdatedAt());
        res.setResolvedAt(incident.getResolvedAt());

        List<IncidentComment> comments = commentRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId());
        res.setCommentsCount(comments.size());
        res.setComments(comments.stream()
                .map(c -> new IncidentResponse.CommentDto(c.getId(), c.getUser().getId(), c.getUser().getUsername(), c.getCommentText(), c.getCreatedAt()))
                .collect(Collectors.toList()));

        return res;
    }
}
