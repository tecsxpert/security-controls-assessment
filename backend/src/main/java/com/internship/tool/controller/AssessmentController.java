package com.internship.tool.controller;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import com.internship.tool.service.AssessmentService;
import com.internship.tool.service.ExportService;
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

    private final AssessmentService service;
    private final AssessmentRepository repo;
    private final ExportService exportService;

    // ✅ CREATE
    @PostMapping("/create")
    public Assessment create(@RequestBody(required = false) Assessment a) {

        if (a == null) {
            throw new IllegalArgumentException("Invalid request body");
        }

        return service.create(a);
    }

    // ✅ 1. UPDATE (PUT)
    @PutMapping("/{id}")
    public Assessment update(@PathVariable Long id, @RequestBody Assessment updated) {

        Assessment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setScore(updated.getScore());
        existing.setCategory(updated.getCategory());
        existing.setCreatedBy(updated.getCreatedBy());

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
        return repo.optimizedSearch(q);
    }


    // ✅ 4. FILTER BY STATUS
    @GetMapping("/status")
    public Page<Assessment> filterByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return repo.findByStatus(status, PageRequest.of(page, size));
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

    // ✅ 7. EXPORT CSV
    @GetMapping(value = "/export", produces = "text/csv")
    public String exportCsv() {
        return exportService.generateCSV();
    }
}
