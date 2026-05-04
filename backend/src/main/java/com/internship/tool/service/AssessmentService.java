package com.internship.tool.service;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository repo;

    // ✅ CREATE with validation
    public Assessment create(Assessment a) {

        if (a == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (a.getName() == null || a.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (a.getScore() < 0 || a.getScore() > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }

        if (a.getStatus() == null) {
            a.setStatus("PENDING"); // default
        }

        return repo.save(a);
    }

    // ✅ UPDATE with validation
    public Assessment update(Long id, Assessment updated) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // ✅ Prevent null overwrite
        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }

        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        if (updated.getScore() >= 0 && updated.getScore() <= 100) {
            existing.setScore(updated.getScore());
        }

        if (updated.getCategory() != null) {
            existing.setCategory(updated.getCategory());
        }

        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }

        if (updated.getCreatedBy() != null) {
            existing.setCreatedBy(updated.getCreatedBy());
        }

        return repo.save(existing);
    }
}
