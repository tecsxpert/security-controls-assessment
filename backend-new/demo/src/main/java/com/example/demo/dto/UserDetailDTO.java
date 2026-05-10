package com.example.demo.dto;

public class UserDetailDTO {

    private Long id;
    private String name;
    private String email;
    private String status;
    private String role;
    private Integer score;
    private String aiAnalysis;

    public UserDetailDTO(Long id,
                         String name,
                         String email,
                         String status,
                         String role,
                         Integer score,
                         String aiAnalysis) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.role = role;
        this.score = score;
        this.aiAnalysis = aiAnalysis;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }

    public Integer getScore() {
        return score;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }
}