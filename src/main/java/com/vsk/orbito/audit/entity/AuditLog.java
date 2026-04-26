package com.vsk.orbito.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who made the request
    @Column
    private String userEmail;

    // which method was called
    @Column(nullable = false)
    private String className;

    @Column(nullable = false)
    private String methodName;

    // HTTP method + URL
    @Column
    private String httpMethod;

    @Column
    private String requestUrl;

    // how long it took in milliseconds
    @Column
    private Long executionTimeMs;

    // did it succeed or throw an exception
    @Column(nullable = false)
    private String status; // SUCCESS or FAILED

    // if failed — what was the error
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}