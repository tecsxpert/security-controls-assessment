package com.internship.tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * SECURITY NOTE:
 * - Swagger UI and API docs are NOT public — require JWT token.
 * - Only /api/auth/login and /api/auth/register are truly public.
 * - All other endpoints require authentication.
 * - Day 5: Java Developer 1 wires JwtAuthFilter here.
 * - Day 6: Java Developer 2 adds @PreAuthorize RBAC.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller / service methods
public class SecurityConfig {

    /**
     * Only these endpoints are truly public — no JWT required.
     * Swagger and api-docs are intentionally excluded to prevent
     * unauthenticated API discovery (flagged by OWASP ZAP).
     */
    private static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health"     // health check only — no sensitive data
    };

    /**
     * Swagger/OpenAPI — accessible only in non-production profiles.
     * Requires authentication in all environments.
     * Access via: Authorization: Bearer <token> in Swagger UI.
     */
    private static final String[] SWAGGER_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    // Swagger requires authentication — prevents unauthenticated API discovery
                    .requestMatchers(SWAGGER_URLS).authenticated()
                    // All other requests require a valid JWT
                    .anyRequest().authenticated()
            )
            // Disable default form login and HTTP Basic — JWT only
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        // TODO Day 5: add JwtAuthFilter before UsernamePasswordAuthenticationFilter
        // http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
