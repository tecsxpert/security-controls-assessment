package com.internship.tool.controller;

import com.internship.tool.dto.ApiResponse;
import com.internship.tool.dto.SecurityControlRequest;
import com.internship.tool.dto.SecurityControlResponse;
import com.internship.tool.entity.SecurityControl;
import com.internship.tool.service.SecurityControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tool-53 — Security Controls Assessment
 * REST Controller — Day 4 (Java Developer 1)
 *
 * SECURITY NOTES:
 * - All endpoints require JWT token (enforced in SecurityConfig)
 * - @AuthenticationPrincipal extracts user from JWT — never trust request body for username
 * - Input validated with @Valid before reaching service layer
 * - Paginated responses prevent large data dumps
 * - Never return stack traces — handled by @ControllerAdvice (Day 8)
 */
@RestController
@RequestMapping("/api/controls")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Security Controls", description = "CRUD endpoints for Security Controls Assessment")
@SecurityRequirement(name = "bearerAuth")   // all endpoints require JWT
public class SecurityControlController {

    private final SecurityControlService service;

    // ── GET /api/controls/all — paginated list ────────────────────────────────

    @GetMapping("/all")
    @Operation(summary = "Get all security controls (paginated)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — JWT missing or invalid")
    public ResponseEntity<ApiResponse<Page<SecurityControlResponse>>> getAll(
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SecurityControlResponse> result = service.getAll(pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.success("Controls fetched successfully", result));
    }

    // ── GET /api/controls/{id} — single record ────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a security control by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Control found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Control not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<SecurityControlResponse>> getById(
            @PathVariable @Min(1) Long id
    ) {
        SecurityControl control = service.getById(id);  // throws ResourceNotFoundException → 404
        return ResponseEntity.ok(ApiResponse.success("Control fetched successfully", toResponse(control)));
    }

    // ── POST /api/controls/create ─────────────────────────────────────────────

    @PostMapping("/create")
    @Operation(summary = "Create a new security control")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Control created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate control ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<SecurityControlResponse>> create(
            @Valid @RequestBody SecurityControlRequest request,
            @AuthenticationPrincipal UserDetails userDetails   // username from JWT — never from request body
    ) {
        String createdBy = userDetails != null ? userDetails.getUsername() : "system";
        SecurityControl created = service.create(toEntity(request), createdBy);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Control created successfully", toResponse(created)));
    }

    // ── GET /api/controls/stats — dashboard KPIs ──────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard KPI stats")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stats returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats fetched successfully", service.getStats()));
    }

    // ── PRIVATE: Entity ↔ DTO mappers ─────────────────────────────────────────

    private SecurityControl toEntity(SecurityControlRequest req) {
        return SecurityControl.builder()
                .controlName(req.getControlName())
                .controlId(req.getControlId())
                .description(req.getDescription())
                .category(req.getCategory())
                .status(SecurityControl.ControlStatus.valueOf(req.getStatus()))
                .riskLevel(SecurityControl.RiskLevel.valueOf(req.getRiskLevel()))
                .score(req.getScore())
                .owner(req.getOwner())
                .department(req.getDepartment())
                .assessmentDate(req.getAssessmentDate())
                .nextReviewDate(req.getNextReviewDate())
                .evidence(req.getEvidence())
                .remediationPlan(req.getRemediationPlan())
                .build();
    }

    private SecurityControlResponse toResponse(SecurityControl c) {
        return SecurityControlResponse.builder()
                .id(c.getId())
                .controlName(c.getControlName())
                .controlId(c.getControlId())
                .description(c.getDescription())
                .category(c.getCategory())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .riskLevel(c.getRiskLevel() != null ? c.getRiskLevel().name() : null)
                .score(c.getScore())
                .owner(c.getOwner())
                .department(c.getDepartment())
                .assessmentDate(c.getAssessmentDate())
                .nextReviewDate(c.getNextReviewDate())
                .evidence(c.getEvidence())
                .remediationPlan(c.getRemediationPlan())
                .aiDescription(c.getAiDescription())
                .aiRecommendations(c.getAiRecommendations())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
