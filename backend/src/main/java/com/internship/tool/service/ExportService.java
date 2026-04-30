package com.internship.tool.service;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final AssessmentRepository repo;

    public String generateCSV() {

        List<Assessment> list = repo.findAll();

        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("ID,Name,Description,Status,Score,Category\n");

        // Data rows (exclude soft deleted records)
        for (Assessment a : list) {
            if ("DELETED".equalsIgnoreCase(a.getStatus())) {
                continue;
            }
            csv.append(escapeCsv(a.getId()))
               .append(",")
               .append(escapeCsv(a.getName()))
               .append(",")
               .append(escapeCsv(a.getDescription()))
               .append(",")
               .append(escapeCsv(a.getStatus()))
               .append(",")
               .append(escapeCsv(a.getScore()))
               .append(",")
               .append(escapeCsv(a.getCategory()))
               .append("\n");
        }

        return csv.toString();

    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\n") || text.contains("\r") || text.contains("\"")) {
            text = text.replace("\"", "\"\"");
            return "\"" + text + "\"";
        }
        return text;
    }
}