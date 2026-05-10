package com.example.demo.dto;

public class MonthlyTrendDTO {

    private String month;
    private Long total;

    public MonthlyTrendDTO(String month, Long total) {
        this.month = month;
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public Long getTotal() {
        return total;
    }
}