package com.securitycontrols.assessment.api;

import java.util.Map;

import com.securitycontrols.assessment.integration.AIServiceClient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
public class AIProxyController {
    private final AIServiceClient aiServiceClient;

    public AIProxyController(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping("/health")
    public Mono<Map> health() {
        return aiServiceClient.health();
    }

    @PostMapping("/describe")
    public Mono<Map> describe(@Valid @RequestBody DescribeRequest request) {
        return aiServiceClient.describe(request.control());
    }

    @PostMapping("/categorise")
    public Mono<Map> categorise(@Valid @RequestBody CategoriseRequest request) {
        return aiServiceClient.categorise(request.text());
    }

    @PostMapping("/recommend")
    public Mono<Map> recommend(@Valid @RequestBody RecommendRequest request) {
        return aiServiceClient.recommend(request.finding(), request.context() == null ? Map.of() : request.context());
    }

    @PostMapping("/generate-report")
    public Mono<Map> generateReport(@Valid @RequestBody ReportRequest request) {
        return aiServiceClient.generateReport(request.assessment());
    }

    public record DescribeRequest(@NotBlank String control) {}
    public record CategoriseRequest(@NotBlank String text) {}
    public record RecommendRequest(@NotBlank String finding, Map<String, Object> context) {}
    public record ReportRequest(@NotNull Map<String, Object> assessment) {}
}
