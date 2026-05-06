package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tool-53 — Security Controls Assessment
 * Main JPA Entity — maps to the "security_controls" table in PostgreSQL.
 * Schema is created by Flyway V1 migration (Java Developer 2).
 *
 * Updated: Added aiDescription (length 5000) and aiStatus (length 100)
 * fields with explicit getters/setters as requested by teammate.
 */
@Entity
@Table(name = "security_controls")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityControl {

    // ── Primary Key ───────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core Fields ───────────────────────────────────────────────────────────

    @Column(name = "control_name", nullable = false, length = 255)
    private String controlName;

    @Column(name = "control_id", nullable = false, unique = true, length = 50)
    private String controlId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ControlStatus status;

    @Column(name = "risk_level", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "score")
    private Integer score;

    @Column(name = "owner", length = 150)
    private String owner;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "remediation_plan", columnDefinition = "TEXT")
    private String remediationPlan;

    // ── AI Fields ─────────────────────────────────────────────────────────────

    /**
     * AI-generated description — filled async after record creation.
     * Length 5000 to accommodate detailed AI responses.
     * Added as requested by teammate for AiServiceClient integration.
     */
    @Column(name = "ai_description", length = 5000)
    private String aiDescription;

    /**
     * AI processing status — tracks state of AI analysis.
     * e.g. PENDING, COMPLETED, FAILED, FALLBACK
     * Added as requested by teammate for AiServiceClient integration.
     */
    @Column(name = "ai_status", length = 100)
    private String aiStatus;

    @Column(name = "ai_recommendations", columnDefinition = "TEXT")
    private String aiRecommendations;

    // ── File ──────────────────────────────────────────────────────────────────

    @Column(name = "file_path", length = 500)
    private String filePath;

    // ── Soft Delete ───────────────────────────────────────────────────────────

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // ── Audit Fields ─────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 150, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum ControlStatus {
        COMPLIANT,
        NON_COMPLIANT,
        PARTIAL,
        NOT_ASSESSED
    }

    public enum RiskLevel {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    // ── Explicit Getters and Setters for AI fields ────────────────────────────
    // Added as requested by teammate for AiServiceClient.java (PR #26)

    public String getAiDescription() {
        return aiDescription;
    }

    public void setAiDescription(String aiDescription) {
        this.aiDescription = aiDescription;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }
}
