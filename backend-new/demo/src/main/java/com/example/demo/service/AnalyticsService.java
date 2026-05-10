package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public AnalyticsResponseDTO getAnalytics() {

        List<CategoryChartDTO> categoryData = repository.getCategoryAnalytics()
                .stream()
                .map(row -> new CategoryChartDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                ))
                .toList();

        List<MonthlyTrendDTO> monthlyData = repository.getMonthlyTrend()
                .stream()
                .map(row -> new MonthlyTrendDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                ))
                .toList();

        List<StatusChartDTO> statusData = repository.getStatusAnalytics()
                .stream()
                .map(row -> new StatusChartDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                ))
                .toList();

        return new AnalyticsResponseDTO(categoryData, monthlyData, statusData);
    }
}