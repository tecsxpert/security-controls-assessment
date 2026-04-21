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
 */
@Entity
@Table(name = "security_controls")
@EntityListeners(AuditingEntityListener.class)  // enables @CreatedDate / @LastModifiedDate
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
    private String controlId;             // e.g. "CC-001", "AC-101"

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;             // e.g. "Access Control", "Network Security"

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ControlStatus status;        // COMPLIANT, NON_COMPLIANT, PARTIAL, NOT_ASSESSED

    @Column(name = "risk_level", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;         // CRITICAL, HIGH, MEDIUM, LOW

    @Column(name = "score")
    private Integer score;               // 0 - 100

    @Column(name = "owner", length = 150)
    private String owner;                // Person responsible

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;             // Proof of compliance

    @Column(name = "remediation_plan", columnDefinition = "TEXT")
    private String remediationPlan;      // Steps to fix if non-compliant

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;        // AI-generated description (filled async)

    @Column(name = "ai_recommendations", columnDefinition = "TEXT")
    private String aiRecommendations;    // AI-generated recommendations

    @Column(name = "file_path", length = 500)
    private String filePath;             // Uploaded evidence file path

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;   // Soft delete flag

    // ── Audit Fields (auto-filled by Spring Data JPA Auditing) ───────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 150, updatable = false)
    private String createdBy;            // Set manually from JWT token

    @Column(name = "updated_by", length = 150)
    private String updatedBy;            // Set manually from JWT token

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
}
