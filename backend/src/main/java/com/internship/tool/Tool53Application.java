package com.internship.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tool-53 — Security Controls Assessment
 * Capstone Project | Sprint: 14 April – 9 May 2026
 *
 * Entry point for the Spring Boot backend (port 8080).
 * Swagger UI: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
@EnableJpaAuditing          // Enables @CreatedDate / @LastModifiedDate on entities
@EnableCaching              // Enables @Cacheable / @CacheEvict (backed by Redis)
@EnableAsync                // Enables @Async for non-blocking AI service calls
@EnableScheduling           // Enables @Scheduled cron jobs (reminders, digests)
public class Tool53Application {

    public static void main(String[] args) {
        SpringApplication.run(Tool53Application.class, args);
    }
}
