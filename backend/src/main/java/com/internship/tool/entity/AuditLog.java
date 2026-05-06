package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String action;

    @Column
    private String username;

    @Column(columnDefinition = "TEXT")
    private String oldData;

    @Column(columnDefinition = "TEXT")
    private String newData;

    @Column
    private String methodName;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}