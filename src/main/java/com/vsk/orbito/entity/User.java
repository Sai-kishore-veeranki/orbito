package com.vsk.orbito.entity;

import com.vsk.orbito.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // nullable — OAuth2 users have no password
    @Column(columnDefinition = "VARCHAR(255) DEFAULT NULL")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 1")
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false,
            columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column
    private LocalDateTime lockedUntil;

    // OAuth2 fields — all nullable
    @Column(unique = true)
    private String googleId;

    @Column
    private String profilePicture;

    // LOCAL or GOOGLE — always explicitly set
    @Column(nullable = false,
            columnDefinition = "VARCHAR(20) DEFAULT 'LOCAL'")
    @Builder.Default
    private String provider = "LOCAL";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}