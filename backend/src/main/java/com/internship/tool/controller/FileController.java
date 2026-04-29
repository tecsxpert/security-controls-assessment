package com.internship.tool.controller;

import com.internship.tool.dto.ApiResponse;
import com.internship.tool.dto.FileAttachmentResponse;
import com.internship.tool.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Tool-53 — File Attachment Controller (Day 9)
 *
 * POST /api/files/upload        — upload file for a control
 * GET  /api/files/{id}          — download file by ID
 * GET  /api/files/{id}/metadata — get file metadata
 * GET  /api/files/control/{id}  — get all files for a control
 *
 * SECURITY NOTES:
 * - All endpoints require JWT token
 * - Username taken from JWT — never from request params
 * - File served with Content-Disposition: attachment — prevents browser execution
 * - Content-Type set from DB record — never from client request
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Attachments", description = "Upload and download file attachments")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    // ── POST /api/files/upload ────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment for a security control")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File uploaded")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file type or size")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<FileAttachmentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("controlId") Long controlId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws Exception {

        String uploadedBy = userDetails != null ? userDetails.getUsername() : "system";
        FileAttachmentResponse response = fileService.upload(file, controlId, uploadedBy);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    // ── GET /api/files/{id} — download file ───────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Download a file by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "File not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {

        FileAttachmentResponse metadata = fileService.getMetadata(id);
        Resource resource = fileService.download(id);

        return ResponseEntity.ok()
                // SECURITY: force download — prevent browser from executing file
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                // SECURITY: content type from DB — never trust client
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(resource);
    }

    // ── GET /api/files/{id}/metadata ─────────────────────────────────────────

    @GetMapping("/{id}/metadata")
    @Operation(summary = "Get file metadata by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Metadata returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "File not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<FileAttachmentResponse>> getMetadata(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("File metadata fetched", fileService.getMetadata(id)));
    }

    // ── GET /api/files/control/{controlId} ───────────────────────────────────

    @GetMapping("/control/{controlId}")
    @Operation(summary = "Get all files for a security control")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Files returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<List<FileAttachmentResponse>>> getByControl(
            @PathVariable Long controlId) {

        return ResponseEntity.ok(
                ApiResponse.success("Files fetched successfully",
                        fileService.getByControlId(controlId)));
    }
}
