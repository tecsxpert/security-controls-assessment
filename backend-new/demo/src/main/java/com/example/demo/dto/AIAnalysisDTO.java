package com.example.demo.dto;

public class AIAnalysisDTO {

    private Long userId;
    private int score;
    private String riskLevel;
    private String summary;
    private String recommendation;
    private String generatedAt;

    public AIAnalysisDTO(
            Long userId,
            int score,
            String riskLevel,
            String summary,
            String recommendation,
            String generatedAt
    ) {
        this.userId = userId;
        this.score = score;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.recommendation = recommendation;
        this.generatedAt = generatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }
}