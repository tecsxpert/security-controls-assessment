package com.internship.tool.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for SecurityControl.
 * SECURITY: Never expose internal fields like isDeleted, filePath, or raw AI fields.
 * Only return what the frontend needs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityControlResponse {

    private Long id;
    private String controlName;
    private String controlId;
    private String description;
    private String category;
    private String status;
    private String riskLevel;
    private Integer score;
    private String owner;
    private String department;
    private LocalDateTime assessmentDate;
    private LocalDateTime nextReviewDate;
    private String evidence;
    private String remediationPlan;
    private String aiDescription;
    private String aiRecommendations;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
