package com.internship.tool.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Tool-53 — Unit Tests for JwtUtil (Day 11)
 * Tests token generation, validation, and extraction.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Valid Base64-encoded secret — 32+ characters
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                "tool53-test-secret-key-32-chars!!".getBytes());

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",        TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs",  86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("generateToken() — should return non-null token")
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("admin@tool53.com", "ADMIN");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("extractUsername() — should return correct username from token")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("admin@tool53.com", "ADMIN");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin@tool53.com");
    }

    @Test
    @DisplayName("extractRole() — should return correct role from token")
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("admin@tool53.com", "ADMIN");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("validateToken() — should return true for valid token")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("admin@tool53.com", "ADMIN");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken() — should return false for tampered token")
    void validateToken_tamperedToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("validateToken() — should return false for empty string")
    void validateToken_emptyToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired() — should return false for fresh token")
    void isTokenExpired_freshToken_returnsFalse() {
        String token = jwtUtil.generateToken("admin@tool53.com", "ADMIN");
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken() — should return non-null refresh token")
    void generateRefreshToken_returnsNonNullToken() {
        String token = jwtUtil.generateRefreshToken("admin@tool53.com");
        assertThat(token).isNotNull().isNotBlank();
    }
}
