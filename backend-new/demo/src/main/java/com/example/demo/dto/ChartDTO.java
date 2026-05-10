package com.example.demo.dto;

public class ChartDTO {

    private String name;
    private int value;

    public ChartDTO() {
    }

    public ChartDTO(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
