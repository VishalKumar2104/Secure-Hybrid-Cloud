package com.sernms.dto;

import jakarta.validation.constraints.NotBlank;

public class IncidentCommentRequest {
    @NotBlank(message = "Comment text is required")
    private String commentText;

    public IncidentCommentRequest() {}

    public IncidentCommentRequest(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}
