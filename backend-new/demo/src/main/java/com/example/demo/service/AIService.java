package com.example.demo.service;

import com.example.demo.dto.AIAnalysisDTO;
import com.example.demo.exception.AIServiceException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AIService {

    public AIAnalysisDTO analyzeUser(Long userId) {

        // Loading delay simulation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Validation
        if (userId <= 0) {
            throw new AIServiceException(
                    "Invalid user id"
            );
        }

        // AI Response
        return new AIAnalysisDTO(
                userId,
                92,
                "LOW",
                "Highly active premium user",
                "Offer loyalty rewards",
                LocalDateTime.now().toString()
        );
    }
}