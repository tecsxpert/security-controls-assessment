package com.example.demo.dto;

public class CategoryChartDTO {

    private String category;
    private Long total;

    public CategoryChartDTO(String category, Long total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public Long getTotal() {
        return total;
    }
}