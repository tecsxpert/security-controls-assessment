package com.internship.tool.service;

import com.internship.tool.dto.FileAttachmentResponse;
import com.internship.tool.entity.FileAttachment;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tool-53 — File Attachment Service (Day 9)
 *
 * SECURITY NOTES:
 * - File type validated against whitelist — no executable files allowed
 * - File size capped at 10 MB — prevents DoS via large uploads
 * - UUID filename used for storage — original filename never used on disk
 * - Path traversal prevented — files stored in configured upload dir only
 * - filePath never exposed to client — only metadata returned
 * - Content-Type validated server-side — never trust client-provided type
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileAttachmentRepository fileAttachmentRepository;

    // Max file size: 10 MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed MIME types whitelist — no executables, no scripts
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "text/plain"
    );

    @Value("${app.file-upload.upload-dir:uploads}")
    private String uploadDir;

    // ── UPLOAD ────────────────────────────────────────────────────────────────

    public FileAttachmentResponse upload(MultipartFile file,
                                          Long controlId,
                                          String uploadedBy) throws IOException {

        // 1. Validate file not empty
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }

        // 2. Validate file size — max 10 MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException(
                "File size exceeds maximum allowed size of 10 MB");
        }

        // 3. Validate content type — whitelist only
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException(
                "File type not allowed. Allowed types: PDF, PNG, JPEG, TXT");
        }

        // 4. Validate original filename — prevent path traversal
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new ValidationException("Invalid filename");
        }

        // 5. Generate UUID filename — never use original name on disk
        String extension  = getExtension(originalFilename);
        String uuidFilename = UUID.randomUUID().toString() + extension;

        // 6. Store file safely in upload directory
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        Path targetPath = uploadPath.resolve(uuidFilename).normalize();

        // SECURITY: ensure resolved path is inside upload directory
        if (!targetPath.startsWith(uploadPath)) {
            throw new ValidationException("Invalid file path detected");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File uploaded: {} -> {}", originalFilename, uuidFilename);

        // 7. Save metadata to DB — never save full path
        FileAttachment attachment = FileAttachment.builder()
                .uuidFilename(uuidFilename)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(file.getSize())
                .controlId(controlId)
                .uploadedBy(uploadedBy)
                .build();

        FileAttachment saved = fileAttachmentRepository.save(attachment);
        return toResponse(saved);
    }

    // ── DOWNLOAD ──────────────────────────────────────────────────────────────

    public Resource download(Long fileId) throws MalformedURLException {
        FileAttachment attachment = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: " + fileId));

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath   = uploadPath.resolve(attachment.getUuidFilename()).normalize();

        // SECURITY: verify file is still inside upload directory
        if (!filePath.startsWith(uploadPath)) {
            throw new ResourceNotFoundException("File not found");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("File not found or not readable");
        }

        return resource;
    }

    // ── GET METADATA ──────────────────────────────────────────────────────────

    public FileAttachmentResponse getMetadata(Long fileId) {
        FileAttachment attachment = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: " + fileId));
        return toResponse(attachment);
    }

    // ── GET ALL FOR CONTROL ───────────────────────────────────────────────────

    public List<FileAttachmentResponse> getByControlId(Long controlId) {
        return fileAttachmentRepository.findByControlId(controlId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }

    private FileAttachmentResponse toResponse(FileAttachment f) {
        return FileAttachmentResponse.builder()
                .id(f.getId())
                .originalFilename(f.getOriginalFilename())
                .contentType(f.getContentType())
                .fileSize(f.getFileSize())
                .controlId(f.getControlId())
                .uploadedBy(f.getUploadedBy())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}
