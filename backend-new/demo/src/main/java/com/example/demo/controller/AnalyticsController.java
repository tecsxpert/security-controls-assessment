package com.example.demo.controller;

import com.example.demo.dto.AnalyticsResponseDTO;
import com.example.demo.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsResponseDTO getAnalytics() {
        return analyticsService.getAnalytics();
    }
}