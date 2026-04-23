package com.internship.tool.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Request DTO for creating / updating a SecurityControl.
 * All validation annotations are here — entity stays clean.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityControlRequest {

    @NotBlank(message = "Control name is required")
    @Size(max = 255, message = "Control name must not exceed 255 characters")
    private String controlName;

    @NotBlank(message = "Control ID is required")
    @Size(max = 50, message = "Control ID must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}$",
             message = "Control ID must follow format: CC-001")
    private String controlId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Status is required")
    private String status;           // COMPLIANT | NON_COMPLIANT | PARTIAL | NOT_ASSESSED

    @NotNull(message = "Risk level is required")
    private String riskLevel;        // CRITICAL | HIGH | MEDIUM | LOW

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    private Integer score;

    @Size(max = 150, message = "Owner name must not exceed 150 characters")
    private String owner;

    @Size(max = 150, message = "Department must not exceed 150 characters")
    private String department;

    private LocalDateTime assessmentDate;

    private LocalDateTime nextReviewDate;

    private String evidence;

    private String remediationPlan;
}
