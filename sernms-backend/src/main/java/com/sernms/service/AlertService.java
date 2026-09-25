package com.sernms.service;

import com.sernms.dto.AlertDto;
import com.sernms.entity.Alert;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public AlertService(AlertRepository alertRepository, NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AlertDto> getAllAlerts() {
        return alertRepository.findRecentAlerts().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertDto> getActiveAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByTriggeredAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertDto createAlert(String title, String message, String severity, String category,
                                String sourceEntityType, String sourceEntityId) {
        Alert alert = new Alert(title, message, severity, category, sourceEntityType, sourceEntityId);
        Alert saved = alertRepository.save(alert);

        // Also broadcast notification to dashboard
        notificationService.createGlobalNotification(title, message, severity.equalsIgnoreCase("CRITICAL") ? "ALERT" : "WARNING");

        return mapToDto(saved);
    }

    @Transactional
    public AlertDto acknowledgeAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setAcknowledged(true);
        Alert updated = alertRepository.save(alert);
        return mapToDto(updated);
    }

    @Transactional
    public AlertDto resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setAcknowledged(true);
        alert.setResolvedAt(LocalDateTime.now());
        Alert updated = alertRepository.save(alert);
        return mapToDto(updated);
    }

    public AlertDto mapToDto(Alert alert) {
        AlertDto dto = new AlertDto();
        dto.setId(alert.getId());
        dto.setAlertTitle(alert.getAlertTitle());
        dto.setAlertMessage(alert.getAlertMessage());
        dto.setSeverity(alert.getSeverity());
        dto.setCategory(alert.getCategory());
        dto.setSourceEntityType(alert.getSourceEntityType());
        dto.setSourceEntityId(alert.getSourceEntityId());
        dto.setAcknowledged(alert.isAcknowledged());
        dto.setTriggeredAt(alert.getTriggeredAt());
        dto.setResolvedAt(alert.getResolvedAt());
        return dto;
    }
}
