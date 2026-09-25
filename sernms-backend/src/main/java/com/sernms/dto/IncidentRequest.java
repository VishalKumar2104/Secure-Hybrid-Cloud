package com.sernms.dto;

import jakarta.validation.constraints.NotBlank;

public class IncidentRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category; // NETWORK, CLOUD, SECURITY, DATABASE, HARDWARE

    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    private String impactedResourceType;
    private String impactedResourceId;
    private Long assigneeId;

    public IncidentRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getImpactedResourceType() { return impactedResourceType; }
    public void setImpactedResourceType(String impactedResourceType) { this.impactedResourceType = impactedResourceType; }

    public String getImpactedResourceId() { return impactedResourceId; }
    public void setImpactedResourceId(String impactedResourceId) { this.impactedResourceId = impactedResourceId; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
}
