package com.internship.tool.controller;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentRepository repo;

    // ✅ 1. UPDATE (PUT)
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
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        existing.setStatus("DELETED"); // soft delete
        repo.save(existing);

        return "Assessment marked as deleted";
    }


    // ✅ 3. SEARCH API
    @GetMapping("/search")
    public List<Assessment> search(@RequestParam String q) {
        return repo.searchByKeyword(q);
    }


    // ✅ 4. FILTER BY STATUS
    @GetMapping("/status")
    public List<Assessment> filterByStatus(@RequestParam String status) {
        return repo.findByStatus(status);
    }


    // ✅ 5. PAGINATION + SORT
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


    // ✅ 6. STATS API (Dashboard KPIs)
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