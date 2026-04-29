package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tool-53 — FileAttachment Entity
 * Stores metadata of uploaded files linked to a SecurityControl.
 *
 * SECURITY NOTES:
 * - Actual file stored with UUID filename — original name never used for storage
 * - File type and size validated before saving
 * - filePath never exposed in API response
 */
@Entity
@Table(name = "file_attachments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid_filename", nullable = false, unique = true)
    private String uuidFilename;        // UUID-based name used for actual storage

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;    // Original name shown to user

    @Column(name = "content_type", nullable = false)
    private String contentType;         // e.g. application/pdf

    @Column(name = "file_size", nullable = false)
    private Long fileSize;              // in bytes

    @Column(name = "control_id", nullable = false)
    private Long controlId;             // linked SecurityControl ID

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;          // from JWT token

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
