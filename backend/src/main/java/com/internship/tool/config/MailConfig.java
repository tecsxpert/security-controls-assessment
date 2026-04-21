package com.internship.tool.config;

import org.springframework.context.annotation.Configuration;

/**
 * Mail configuration is handled entirely through application.yml
 * (spring.mail.* properties) and the .env file.
 *
 * JavaMailSender is auto-configured by spring-boot-starter-mail.
 * Thymeleaf email templates go in: src/main/resources/templates/
 *
 * Java Developer 1 wires actual email sending on Day 7.
 */
@Configuration
public class MailConfig {
    // Spring Boot auto-configures JavaMailSender from application.yml.
    // No extra beans needed here.
}
