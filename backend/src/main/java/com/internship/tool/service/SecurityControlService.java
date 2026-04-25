package com.internship.tool.service;

import com.internship.tool.config.RedisConfig;
import com.internship.tool.entity.SecurityControl;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.SecurityControlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool-53 — Security Controls Service (Day 6 — Redis Caching added)
 *
 * CACHING STRATEGY:
 * - @Cacheable on all GET methods — returns cached result if available (10 min TTL)
 * - @CacheEvict on all writes (create, update, delete) — keeps cache in sync
 * - Stats cache evicted on every write — dashboard always shows fresh counts
 *
 * SECURITY NOTES:
 * - Input validation before any DB operation
 * - Soft delete — records never permanently removed
 * - createdBy/updatedBy set from JWT token, never from request body
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityControlService {

    private final SecurityControlRepository repository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_CONTROLS, allEntries = true),
        @CacheEvict(value = RedisConfig.CACHE_STATS,    allEntries = true)
    })
    public SecurityControl create(SecurityControl control, String createdBy) {
        log.info("Creating security control: {}", control.getControlId());

        validateControl(control);

        if (repository.existsByControlId(control.getControlId())) {
            throw new DuplicateResourceException(
                "Control ID already exists: " + control.getControlId());
        }

        control.setCreatedBy(createdBy);
        control.setUpdatedBy(createdBy);
        control.setIsDeleted(false);

        SecurityControl saved = repository.save(control);
        log.info("Security control created with id: {}", saved.getId());
        return saved;
    }

    // ── READ — Get by ID ──────────────────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_CONTROL, key = "#id")
    @Transactional(readOnly = true)
    public SecurityControl getById(Long id) {
        log.debug("Fetching control by id: {} (cache miss)", id);
        return repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security control not found with id: " + id));
    }

    // ── READ — Get All (paginated) ────────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_CONTROLS,
               key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<SecurityControl> getAll(Pageable pageable) {
        log.debug("Fetching all controls page {} (cache miss)", pageable.getPageNumber());
        return repository.findAllByIsDeletedFalse(pageable);
    }

    // ── READ — Search ─────────────────────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_CONTROLS,
               key = "'search-' + #query + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<SecurityControl> search(String query, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            return getAll(pageable);
        }
        return repository.searchByKeyword(query.trim(), pageable);
    }

    // ── READ — Filter by Status ───────────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_CONTROLS,
               key = "'status-' + #status + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<SecurityControl> getByStatus(SecurityControl.ControlStatus status,
                                              Pageable pageable) {
        return repository.findByStatusAndIsDeletedFalse(status, pageable);
    }

    // ── READ — Filter by Risk Level ───────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_CONTROLS,
               key = "'risk-' + #riskLevel + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<SecurityControl> getByRiskLevel(SecurityControl.RiskLevel riskLevel,
                                                 Pageable pageable) {
        return repository.findByRiskLevelAndIsDeletedFalse(riskLevel, pageable);
    }

    // ── READ — Dashboard Stats ────────────────────────────────────────────────

    @Cacheable(value = RedisConfig.CACHE_STATS, key = "'dashboard-stats'")
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        log.debug("Fetching stats (cache miss)");
        Map<String, Object> stats = new HashMap<>();
        stats.put("total",       repository.countByIsDeletedFalse());
        stats.put("compliant",   repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.COMPLIANT));
        stats.put("nonCompliant", repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.NON_COMPLIANT));
        stats.put("partial",     repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.PARTIAL));
        stats.put("notAssessed", repository.countByStatusAndIsDeletedFalse(
                SecurityControl.ControlStatus.NOT_ASSESSED));
        stats.put("critical",    repository.countByRiskLevelAndIsDeletedFalse(
                SecurityControl.RiskLevel.CRITICAL));
        stats.put("high",        repository.countByRiskLevelAndIsDeletedFalse(
                SecurityControl.RiskLevel.HIGH));
        stats.put("averageScore", repository.findAverageScore());
        return stats;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_CONTROL,   key = "#id"),
        @CacheEvict(value = RedisConfig.CACHE_CONTROLS,  allEntries = true),
        @CacheEvict(value = RedisConfig.CACHE_STATS,     allEntries = true)
    })
    public SecurityControl update(Long id, SecurityControl updated, String updatedBy) {
        log.info("Updating security control id: {}", id);

        SecurityControl existing = getById(id);
        validateControl(updated);

        if (!existing.getControlId().equals(updated.getControlId()) &&
                repository.existsByControlId(updated.getControlId())) {
            throw new DuplicateResourceException(
                "Control ID already exists: " + updated.getControlId());
        }

        existing.setControlName(updated.getControlName());
        existing.setControlId(updated.getControlId());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setStatus(updated.getStatus());
        existing.setRiskLevel(updated.getRiskLevel());
        existing.setScore(updated.getScore());
        existing.setOwner(updated.getOwner());
        existing.setDepartment(updated.getDepartment());
        existing.setAssessmentDate(updated.getAssessmentDate());
        existing.setNextReviewDate(updated.getNextReviewDate());
        existing.setEvidence(updated.getEvidence());
        existing.setRemediationPlan(updated.getRemediationPlan());
        existing.setUpdatedBy(updatedBy);

        return repository.save(existing);
    }

    // ── SOFT DELETE ───────────────────────────────────────────────────────────

    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_CONTROL,   key = "#id"),
        @CacheEvict(value = RedisConfig.CACHE_CONTROLS,  allEntries = true),
        @CacheEvict(value = RedisConfig.CACHE_STATS,     allEntries = true)
    })
    public void delete(Long id, String deletedBy) {
        log.info("Soft deleting security control id: {}", id);
        SecurityControl control = getById(id);
        control.setIsDeleted(true);
        control.setUpdatedBy(deletedBy);
        repository.save(control);
    }

    // ── UPDATE AI FIELDS (called async) ──────────────────────────────────────

    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_CONTROL,  key = "#id"),
        @CacheEvict(value = RedisConfig.CACHE_CONTROLS, allEntries = true)
    })
    public void updateAiFields(Long id, String aiDescription, String aiRecommendations) {
        repository.findByIdAndIsDeletedFalse(id).ifPresent(control -> {
            control.setAiDescription(aiDescription);
            control.setAiRecommendations(aiRecommendations);
            repository.save(control);
            log.info("AI fields updated for control id: {}", id);
        });
    }

    // ── PRIVATE VALIDATION ────────────────────────────────────────────────────

    private void validateControl(SecurityControl control) {
        if (!StringUtils.hasText(control.getControlName())) {
            throw new ValidationException("Control name is required");
        }
        if (!StringUtils.hasText(control.getControlId())) {
            throw new ValidationException("Control ID is required");
        }
        if (!StringUtils.hasText(control.getDescription())) {
            throw new ValidationException("Description is required");
        }
        if (!StringUtils.hasText(control.getCategory())) {
            throw new ValidationException("Category is required");
        }
        if (control.getStatus() == null) {
            throw new ValidationException("Status is required");
        }
        if (control.getRiskLevel() == null) {
            throw new ValidationException("Risk level is required");
        }
        if (control.getScore() != null &&
                (control.getScore() < 0 || control.getScore() > 100)) {
            throw new ValidationException("Score must be between 0 and 100");
        }
        if (control.getNextReviewDate() != null &&
                control.getAssessmentDate() != null &&
                control.getNextReviewDate().isBefore(control.getAssessmentDate())) {
            throw new ValidationException(
                "Next review date cannot be before assessment date");
        }
    }
}
