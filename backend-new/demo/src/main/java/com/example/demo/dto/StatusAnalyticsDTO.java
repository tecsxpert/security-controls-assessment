package com.example.demo.dto;

public class StatusAnalyticsDTO {

    private String status;
    private Long count;

    public StatusAnalyticsDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public Long getCount() {
        return count;
    }
}