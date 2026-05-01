package com.internship.tool.service;

import com.internship.tool.entity.SecurityControl;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.SecurityControlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tool-53 — Unit Tests for SecurityControlService (Day 10)
 * Uses Mockito to mock repository — no DB needed.
 * Covers happy path and error cases for every public method.
 */
@ExtendWith(MockitoExtension.class)
class SecurityControlServiceTest {

    @Mock
    private SecurityControlRepository repository;

    @InjectMocks
    private SecurityControlService service;

    private SecurityControl validControl;

    @BeforeEach
    void setUp() {
        validControl = SecurityControl.builder()
                .id(1L)
                .controlName("Access Control Policy")
                .controlId("AC-001")
                .description("Ensures proper access control measures are in place")
                .category("Access Control")
                .status(SecurityControl.ControlStatus.COMPLIANT)
                .riskLevel(SecurityControl.RiskLevel.HIGH)
                .score(85)
                .owner("John Smith")
                .department("IT Security")
                .isDeleted(false)
                .build();
    }

    // ── Test 1: Create — Happy Path ───────────────────────────────────────────

    @Test
    @DisplayName("create() — should save and return control when input is valid")
    void create_validInput_returnsSavedControl() {
        when(repository.existsByControlId("AC-001")).thenReturn(false);
        when(repository.save(any(SecurityControl.class))).thenReturn(validControl);

        SecurityControl result = service.create(validControl, "admin");

        assertThat(result).isNotNull();
        assertThat(result.getControlId()).isEqualTo("AC-001");
        assertThat(result.getCreatedBy()).isEqualTo("admin");
        verify(repository, times(1)).save(any(SecurityControl.class));
    }

    // ── Test 2: Create — Duplicate Control ID ────────────────────────────────

    @Test
    @DisplayName("create() — should throw DuplicateResourceException when controlId exists")
    void create_duplicateControlId_throwsDuplicateResourceException() {
        when(repository.existsByControlId("AC-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validControl, "admin"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("AC-001");

        verify(repository, never()).save(any());
    }

    // ── Test 3: Create — Missing Control Name ────────────────────────────────

    @Test
    @DisplayName("create() — should throw ValidationException when controlName is blank")
    void create_blankControlName_throwsValidationException() {
        validControl.setControlName("");

        assertThatThrownBy(() -> service.create(validControl, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Control name is required");

        verify(repository, never()).save(any());
    }

    // ── Test 4: Create — Invalid Score ───────────────────────────────────────

    @Test
    @DisplayName("create() — should throw ValidationException when score is out of range")
    void create_invalidScore_throwsValidationException() {
        validControl.setScore(150);

        assertThatThrownBy(() -> service.create(validControl, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Score must be between 0 and 100");
    }

    // ── Test 5: GetById — Happy Path ─────────────────────────────────────────

    @Test
    @DisplayName("getById() — should return control when found")
    void getById_existingId_returnsControl() {
        when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validControl));

        SecurityControl result = service.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getControlId()).isEqualTo("AC-001");
    }

    // ── Test 6: GetById — Not Found ──────────────────────────────────────────

    @Test
    @DisplayName("getById() — should throw ResourceNotFoundException when id not found")
    void getById_nonExistingId_throwsResourceNotFoundException() {
        when(repository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── Test 7: GetAll — Happy Path ───────────────────────────────────────────

    @Test
    @DisplayName("getAll() — should return paginated list of controls")
    void getAll_returnsPageOfControls() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SecurityControl> page = new PageImpl<>(List.of(validControl));
        when(repository.findAllByIsDeletedFalse(pageable)).thenReturn(page);

        Page<SecurityControl> result = service.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ── Test 8: Update — Happy Path ───────────────────────────────────────────

    @Test
    @DisplayName("update() — should update and return control when valid")
    void update_validInput_returnsUpdatedControl() {
        SecurityControl updated = SecurityControl.builder()
                .controlName("Updated Policy")
                .controlId("AC-001")
                .description("Updated description")
                .category("Access Control")
                .status(SecurityControl.ControlStatus.NON_COMPLIANT)
                .riskLevel(SecurityControl.RiskLevel.CRITICAL)
                .score(40)
                .isDeleted(false)
                .build();

        when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validControl));
        when(repository.existsByControlId("AC-001")).thenReturn(false);
        when(repository.save(any())).thenReturn(updated);

        SecurityControl result = service.update(1L, updated, "admin");

        assertThat(result.getControlName()).isEqualTo("Updated Policy");
        assertThat(result.getStatus()).isEqualTo(SecurityControl.ControlStatus.NON_COMPLIANT);
    }

    // ── Test 9: Delete — Happy Path ───────────────────────────────────────────

    @Test
    @DisplayName("delete() — should soft delete control (isDeleted = true)")
    void delete_existingId_setsIsDeletedTrue() {
        when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(validControl));
        when(repository.save(any())).thenReturn(validControl);

        service.delete(1L, "admin");

        assertThat(validControl.getIsDeleted()).isTrue();
        verify(repository, times(1)).save(validControl);
    }

    // ── Test 10: GetStats — Happy Path ────────────────────────────────────────

    @Test
    @DisplayName("getStats() — should return map with all KPI counts")
    void getStats_returnsStatsMap() {
        when(repository.countByIsDeletedFalse()).thenReturn(10L);
        when(repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.COMPLIANT)).thenReturn(5L);
        when(repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.NON_COMPLIANT)).thenReturn(2L);
        when(repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.PARTIAL)).thenReturn(2L);
        when(repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.NOT_ASSESSED)).thenReturn(1L);
        when(repository.countByRiskLevelAndIsDeletedFalse(
                SecurityControl.RiskLevel.CRITICAL)).thenReturn(3L);
        when(repository.countByRiskLevelAndIsDeletedFalse(
                SecurityControl.RiskLevel.HIGH)).thenReturn(4L);
        when(repository.findAverageScore()).thenReturn(72.5);

        Map<String, Object> stats = service.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.get("total")).isEqualTo(10L);
        assertThat(stats.get("compliant")).isEqualTo(5L);
        assertThat(stats.get("averageScore")).isEqualTo(72.5);
    }

    // ── Test 11: Create — Missing Status ─────────────────────────────────────

    @Test
    @DisplayName("create() — should throw ValidationException when status is null")
    void create_nullStatus_throwsValidationException() {
        validControl.setStatus(null);

        assertThatThrownBy(() -> service.create(validControl, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Status is required");
    }

    // ── Test 12: Create — Missing Risk Level ─────────────────────────────────

    @Test
    @DisplayName("create() — should throw ValidationException when riskLevel is null")
    void create_nullRiskLevel_throwsValidationException() {
        validControl.setRiskLevel(null);

        assertThatThrownBy(() -> service.create(validControl, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Risk level is required");
    }
}
