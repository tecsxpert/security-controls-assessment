package com.example.demo.dto;

import java.util.List;

public class AnalyticsResponseDTO {

    private List<CategoryChartDTO> categoryData;
    private List<MonthlyTrendDTO> monthlyData;
    private List<StatusChartDTO> statusData;

    public AnalyticsResponseDTO(
            List<CategoryChartDTO> categoryData,
            List<MonthlyTrendDTO> monthlyData,
            List<StatusChartDTO> statusData
    ) {
        this.categoryData = categoryData;
        this.monthlyData = monthlyData;
        this.statusData = statusData;
    }

    public List<CategoryChartDTO> getCategoryData() {
        return categoryData;
    }

    public List<MonthlyTrendDTO> getMonthlyData() {
        return monthlyData;
    }

    public List<StatusChartDTO> getStatusData() {
        return statusData;
    }
}