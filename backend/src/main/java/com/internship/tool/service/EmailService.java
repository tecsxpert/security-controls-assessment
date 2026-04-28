package com.internship.tool.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Tool-53 — Email Notification Service (Day 7)
 *
 * Sends HTML emails using JavaMailSender + Thymeleaf templates.
 * All email sending is @Async — never blocks the main thread.
 *
 * SECURITY NOTES:
 * - Mail credentials loaded from ENV only — never hardcoded
 * - @Async — email failures never crash the main request
 * - All errors caught and logged — never exposed to client
 * - Email content sanitised through Thymeleaf — prevents HTML injection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    // ── Send email on new control created ────────────────────────────────────

    @Async
    public void sendControlCreatedEmail(String toEmail,
                                         String controlName,
                                         String controlId,
                                         String riskLevel,
                                         String createdBy) {
        try {
            Context context = new Context();
            context.setVariable("controlName", controlName);
            context.setVariable("controlId",   controlId);
            context.setVariable("riskLevel",   riskLevel);
            context.setVariable("createdBy",   createdBy);

            String html = templateEngine.process("email/control-created", context);

            sendHtmlEmail(toEmail,
                    "New Security Control Created: " + controlId,
                    html);

            log.info("Control created email sent to: {}", toEmail);

        } catch (Exception e) {
            // SECURITY: log internally — never propagate email errors to client
            log.error("Failed to send control created email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Send email for overdue review ─────────────────────────────────────────

    @Async
    public void sendOverdueReviewEmail(String toEmail,
                                        String controlName,
                                        String controlId,
                                        String nextReviewDate,
                                        String owner) {
        try {
            Context context = new Context();
            context.setVariable("controlName",    controlName);
            context.setVariable("controlId",      controlId);
            context.setVariable("nextReviewDate", nextReviewDate);
            context.setVariable("owner",          owner);

            String html = templateEngine.process("email/overdue-review", context);

            sendHtmlEmail(toEmail,
                    "OVERDUE: Security Control Review Required — " + controlId,
                    html);

            log.info("Overdue review email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send overdue email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Send 7-day advance review reminder ────────────────────────────────────

    @Async
    public void sendUpcomingReviewEmail(String toEmail,
                                         String controlName,
                                         String controlId,
                                         String nextReviewDate,
                                         String owner) {
        try {
            Context context = new Context();
            context.setVariable("controlName",    controlName);
            context.setVariable("controlId",      controlId);
            context.setVariable("nextReviewDate", nextReviewDate);
            context.setVariable("owner",          owner);

            String html = templateEngine.process("email/upcoming-review", context);

            sendHtmlEmail(toEmail,
                    "Reminder: Security Control Review Due in 7 Days — " + controlId,
                    html);

            log.info("Upcoming review email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send upcoming review email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Private helper — send HTML email ─────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);   // true = isHtml
        mailSender.send(message);
    }
}
