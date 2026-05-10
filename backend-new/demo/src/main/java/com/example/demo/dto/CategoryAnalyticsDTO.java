package com.example.demo.dto;

public class CategoryAnalyticsDTO {

    private String category;
    private Long count;

    public CategoryAnalyticsDTO(String category, Long count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public Long getCount() {
        return count;
    }
}