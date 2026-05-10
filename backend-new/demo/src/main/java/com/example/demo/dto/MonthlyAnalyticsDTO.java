package com.example.demo.dto;

public class MonthlyAnalyticsDTO {

    private String month;
    private Long count;

    public MonthlyAnalyticsDTO(String month, Long count) {
        this.month = month;
        this.count = count;
    }

    public String getMonth() {
        return month;
    }

    public Long getCount() {
        return count;
    }
}