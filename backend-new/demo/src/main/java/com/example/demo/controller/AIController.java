package com.example.demo.controller;

import com.example.demo.dto.AIAnalysisDTO;
import com.example.demo.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/{id}/ai-analysis")
    public AIAnalysisDTO analyzeUser(
            @PathVariable Long id
    ) {

        return aiService.analyzeUser(id);
    }
}