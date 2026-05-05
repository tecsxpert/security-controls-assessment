package com.internship.tool.service;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository repo;

    public Assessment getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    // ✅ CREATE with validation
    public Assessment create(Assessment a) {

        if (a.getName() == null || a.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        a.setName(a.getName().trim());

        if (a.getStatus() != null) {
            a.setStatus(a.getStatus().trim().toUpperCase());
        } else {
            a.setStatus("PENDING");
        }

        if (a.getScore() < 0 || a.getScore() > 100) {
            throw new IllegalArgumentException("Score must be 0-100");
        }

        return repo.save(a);
    }

    // ✅ UPDATE with validation
    public Assessment update(Long id, Assessment updated) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (updated.getName() != null) {
            existing.setName(updated.getName().trim());
        }

        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus().trim().toUpperCase());
        }

        if (updated.getScore() != null &&
            updated.getScore() >= 0 && updated.getScore() <= 100) {
            existing.setScore(updated.getScore());
        }

        return repo.save(existing);
    }
}
