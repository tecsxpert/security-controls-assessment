package com.securitycontrols.assessment.integration;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AIServiceClient {
    private final WebClient webClient;

    public AIServiceClient(
            WebClient.Builder builder,
            @Value("${ai.service.base-url:http://localhost:5000}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<Map> describe(String control) {
        return post("/describe", Map.of("control", control));
    }

    public Mono<Map> categorise(String text) {
        return post("/categorise", Map.of("text", text));
    }

    public Mono<Map> recommend(String finding, Map<String, Object> context) {
        return post("/recommend", Map.of("finding", finding, "context", context));
    }

    public Mono<Map> generateReport(Map<String, Object> assessment) {
        return post("/generate-report", Map.of("assessment", assessment));
    }

    public Flux<String> streamReport(Map<String, Object> assessment) {
        return webClient.post()
                .uri("/generate-report/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(Map.of("assessment", assessment))
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(90));
    }

    public Mono<Map> analyseDocument(String document, Map<String, Object> metadata) {
        return post("/analyse-document", Map.of("document", document, "metadata", metadata));
    }

    private Mono<Map> post(String uri, Map<String, Object> payload) {
        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(45));
    }
}
