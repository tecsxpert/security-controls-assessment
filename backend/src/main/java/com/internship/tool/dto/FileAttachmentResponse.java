package com.internship.tool.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for FileAttachment.
 *
 * SECURITY NOTE:
 * - filePath (actual storage path) is NEVER included in response
 * - Only safe metadata is returned to client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileAttachmentResponse {

    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Long controlId;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
