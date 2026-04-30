package com.internship.tool.controller;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentRepository repo;

    // ✅ 0. CREATE (POST)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public Assessment create(@RequestBody Assessment assessment) {
        return repo.save(assessment);
    }

    // ✅ 1. UPDATE (PUT)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public Assessment update(@PathVariable Long id, @RequestBody Assessment updated) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        existing.setName(updated.getName());
        existing.setStatus(updated.getStatus());
        existing.setScore(updated.getScore());

        return repo.save(existing);

    }


    // ✅ 2. DELETE (Soft Delete)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        existing.setStatus("DELETED"); // soft delete
        repo.save(existing);

        return "Assessment marked as deleted";
    }


    // ✅ 3. GET BY ID
    @GetMapping("/{id}")
    public Assessment getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Assessment not found"));
    }


    // ✅ 4. SEARCH API
    @GetMapping("/search")
    public List<Assessment> search(@RequestParam String q) {
        return repo.searchByKeyword(q);
    }


    // ✅ 5. FILTER BY STATUS
    @GetMapping("/status")
    public List<Assessment> filterByStatus(@RequestParam String status) {
        return repo.findByStatus(status);
    }


    // ✅ 6. PAGINATION + SORT
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    @GetMapping("/all")
    public Page<Assessment> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return repo.findAll(pageable);
    }


    // ✅ 7. STATS API (Dashboard KPIs)
    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        long total = repo.count();
        long completed = repo.findByStatus("COMPLETED").size();
        long pending = repo.findByStatus("PENDING").size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pending", pending);

        return stats;
    }
}
