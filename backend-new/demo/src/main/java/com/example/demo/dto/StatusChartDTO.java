package com.example.demo.dto;

public class StatusChartDTO {

    private String status;
    private Long total;

    public StatusChartDTO(String status, Long total) {
        this.status = status;
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public Long getTotal() {
        return total;
    }
}