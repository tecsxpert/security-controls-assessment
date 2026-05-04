package com.internship.tool.config;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public String extractUsername(String token) {
        // Dummy implementation - in real app, parse JWT
        return "user"; // placeholder
    }
}