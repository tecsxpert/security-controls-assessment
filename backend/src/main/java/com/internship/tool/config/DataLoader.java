package com.internship.tool.config;

import com.internship.tool.entity.SecurityControl;
import com.internship.tool.repository.SecurityControlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tool-53 — Data Seeder (Day 14)
 *
 * Seeds 30 realistic demo records on startup.
 * Covers all statuses and score ranges for Demo Day.
 *
 * SECURITY NOTE:
 * - Only runs when DB is empty — never overwrites existing data
 * - No sensitive data seeded — all demo data only
 * - Not active in 'test' profile — tests use their own data
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final SecurityControlRepository repository;

    @Bean
    @Profile("!test")   // never run in test profile
    public CommandLineRunner seedData() {
        return args -> {
            if (repository.countByIsDeletedFalse() > 0) {
                log.info("Database already has data — skipping seed");
                return;
            }

            log.info("Seeding 30 demo security controls...");
            repository.saveAll(getDemoControls());
            log.info("Seeding complete — 30 records inserted");
        };
    }

    private List<SecurityControl> getDemoControls() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(

            // ── COMPLIANT — HIGH score ────────────────────────────────────────
            build("AC-001", "Access Control Policy",
                  "Ensures all system access is governed by a formal access control policy.",
                  "Access Control", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 92,
                  "Alice Johnson", "IT Security", now.minusDays(10), now.plusDays(80)),

            build("AC-002", "Multi-Factor Authentication",
                  "All privileged accounts must use MFA for system access.",
                  "Access Control", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.CRITICAL, 96,
                  "Bob Williams", "IT Security", now.minusDays(5), now.plusDays(90)),

            build("NS-001", "Network Segmentation",
                  "Critical systems are isolated in separate network segments.",
                  "Network Security", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 88,
                  "Carol Davis", "Network Team", now.minusDays(15), now.plusDays(60)),

            build("DS-001", "Data Encryption at Rest",
                  "All sensitive data stored in the database is encrypted using AES-256.",
                  "Data Security", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.CRITICAL, 95,
                  "David Lee", "Data Team", now.minusDays(20), now.plusDays(70)),

            build("IM-001", "Incident Response Plan",
                  "A documented and tested incident response plan is in place.",
                  "Incident Management", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 90,
                  "Eve Martin", "Security Ops", now.minusDays(8), now.plusDays(85)),

            build("CM-001", "Change Management Process",
                  "All system changes go through a formal change management process.",
                  "Change Management", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.MEDIUM, 83,
                  "Frank Brown", "IT Ops", now.minusDays(12), now.plusDays(75)),

            build("BU-001", "Backup and Recovery",
                  "Daily automated backups with weekly recovery tests.",
                  "Business Continuity", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 91,
                  "Grace Kim", "IT Ops", now.minusDays(6), now.plusDays(88)),

            build("VA-001", "Vulnerability Management",
                  "Monthly vulnerability scans with 30-day remediation SLA.",
                  "Vulnerability Management", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 87,
                  "Henry Zhang", "Security Ops", now.minusDays(3), now.plusDays(92)),

            // ── NON_COMPLIANT — LOW score ────────────────────────────────────
            build("AC-003", "Privileged Access Review",
                  "Quarterly review of all privileged accounts has not been completed.",
                  "Access Control", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.CRITICAL, 22,
                  "Ivan Patel", "IT Security", now.minusDays(60), now.minusDays(5)),

            build("NS-002", "Firewall Rule Review",
                  "Firewall rules have not been reviewed in over 12 months.",
                  "Network Security", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 18,
                  "Julia Chen", "Network Team", now.minusDays(90), now.minusDays(20)),

            build("DS-002", "Data Classification",
                  "Data classification policy exists but is not consistently applied.",
                  "Data Security", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 30,
                  "Kevin Adams", "Data Team", now.minusDays(45), now.minusDays(3)),

            build("AT-001", "Security Awareness Training",
                  "Less than 40% of staff have completed mandatory security training.",
                  "Awareness Training", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.MEDIUM, 15,
                  "Laura Wilson", "HR", now.minusDays(120), now.minusDays(30)),

            build("LG-001", "Audit Log Review",
                  "Security audit logs are collected but not regularly reviewed.",
                  "Logging", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 25,
                  "Mike Taylor", "Security Ops", now.minusDays(55), now.minusDays(10)),

            // ── PARTIAL — MEDIUM score ────────────────────────────────────────
            build("AC-004", "Session Management",
                  "Session timeout is configured on some systems but not all.",
                  "Access Control", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.MEDIUM, 55,
                  "Nancy Moore", "IT Security", now.minusDays(25), now.plusDays(30)),

            build("NS-003", "VPN Configuration",
                  "VPN is deployed but split tunnelling is not consistently disabled.",
                  "Network Security", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.HIGH, 60,
                  "Oscar White", "Network Team", now.minusDays(18), now.plusDays(25)),

            build("DS-003", "Secure File Transfer",
                  "SFTP is used for most transfers but some legacy systems still use FTP.",
                  "Data Security", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.MEDIUM, 50,
                  "Paula Harris", "Data Team", now.minusDays(30), now.plusDays(20)),

            build("CM-002", "Patch Management",
                  "Critical patches applied within SLA but non-critical patches are delayed.",
                  "Change Management", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.HIGH, 65,
                  "Quinn Jackson", "IT Ops", now.minusDays(14), now.plusDays(40)),

            build("IM-002", "Security Event Monitoring",
                  "SIEM is deployed but alert tuning is incomplete — high false positive rate.",
                  "Incident Management", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.HIGH, 58,
                  "Rachel Scott", "Security Ops", now.minusDays(22), now.plusDays(35)),

            build("VA-002", "Penetration Testing",
                  "Annual pen test completed but remediation of findings is only 60% done.",
                  "Vulnerability Management", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.CRITICAL, 48,
                  "Sam Thompson", "Security Ops", now.minusDays(40), now.plusDays(15)),

            build("BC-001", "Disaster Recovery Plan",
                  "DRP exists and is documented but has not been tested in 18 months.",
                  "Business Continuity", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.HIGH, 62,
                  "Tina Garcia", "IT Ops", now.minusDays(35), now.plusDays(10)),

            // ── NOT_ASSESSED ──────────────────────────────────────────────────
            build("CL-001", "Cloud Security Posture",
                  "Cloud environment security controls have not yet been formally assessed.",
                  "Cloud Security", SecurityControl.ControlStatus.NOT_ASSESSED,
                  SecurityControl.RiskLevel.HIGH, null,
                  "Uma Patel", "Cloud Team", null, now.plusDays(45)),

            build("SD-001", "Secure Development Lifecycle",
                  "SDL policy is being drafted — formal assessment pending.",
                  "Software Development", SecurityControl.ControlStatus.NOT_ASSESSED,
                  SecurityControl.RiskLevel.MEDIUM, null,
                  "Victor Clark", "Dev Team", null, now.plusDays(50)),

            build("TP-001", "Third Party Risk Assessment",
                  "Vendor risk assessment framework is under review — not yet assessed.",
                  "Third Party Risk", SecurityControl.ControlStatus.NOT_ASSESSED,
                  SecurityControl.RiskLevel.HIGH, null,
                  "Wendy Lewis", "Procurement", null, now.plusDays(55)),

            build("PR-001", "Privacy Impact Assessment",
                  "PIA process is being established — initial assessments not started.",
                  "Privacy", SecurityControl.ControlStatus.NOT_ASSESSED,
                  SecurityControl.RiskLevel.MEDIUM, null,
                  "Xander Robinson", "Legal", null, now.plusDays(60)),

            build("ID-001", "Identity Governance",
                  "Identity governance framework implementation has not begun.",
                  "Access Control", SecurityControl.ControlStatus.NOT_ASSESSED,
                  SecurityControl.RiskLevel.HIGH, null,
                  "Yasmin Walker", "IT Security", null, now.plusDays(65)),

            // ── CRITICAL risk extras ──────────────────────────────────────────
            build("AC-005", "Zero Trust Architecture",
                  "Zero trust model partially implemented — lateral movement still possible.",
                  "Access Control", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.CRITICAL, 42,
                  "Zara Hall", "IT Security", now.minusDays(28), now.plusDays(18)),

            build("CR-001", "Cryptographic Standards",
                  "Weak cryptographic algorithms (MD5, SHA-1) still in use on legacy APIs.",
                  "Data Security", SecurityControl.ControlStatus.NON_COMPLIANT,
                  SecurityControl.RiskLevel.CRITICAL, 10,
                  "Aaron Young", "Dev Team", now.minusDays(75), now.minusDays(15)),

            build("EP-001", "Endpoint Protection",
                  "EDR solution deployed on all managed endpoints with active monitoring.",
                  "Endpoint Security", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.HIGH, 89,
                  "Beth King", "IT Security", now.minusDays(9), now.plusDays(82)),

            build("DL-001", "Data Loss Prevention",
                  "DLP policies configured for email but not yet for cloud storage.",
                  "Data Security", SecurityControl.ControlStatus.PARTIAL,
                  SecurityControl.RiskLevel.HIGH, 53,
                  "Carl Wright", "Data Team", now.minusDays(32), now.plusDays(22)),

            build("RM-001", "Risk Register Maintenance",
                  "Risk register is maintained and reviewed quarterly by security committee.",
                  "Risk Management", SecurityControl.ControlStatus.COMPLIANT,
                  SecurityControl.RiskLevel.MEDIUM, 85,
                  "Diana Lopez", "Risk Team", now.minusDays(7), now.plusDays(84))
        );
    }

    private SecurityControl build(String controlId,
                                   String controlName,
                                   String description,
                                   String category,
                                   SecurityControl.ControlStatus status,
                                   SecurityControl.RiskLevel riskLevel,
                                   Integer score,
                                   String owner,
                                   String department,
                                   LocalDateTime assessmentDate,
                                   LocalDateTime nextReviewDate) {
        return SecurityControl.builder()
                .controlId(controlId)
                .controlName(controlName)
                .description(description)
                .category(category)
                .status(status)
                .riskLevel(riskLevel)
                .score(score)
                .owner(owner)
                .department(department)
                .assessmentDate(assessmentDate)
                .nextReviewDate(nextReviewDate)
                .isDeleted(false)
                .createdBy("system-seeder")
                .updatedBy("system-seeder")
                .build();
    }
}
