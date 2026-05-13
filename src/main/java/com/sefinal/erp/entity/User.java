package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private Long companyId;

    private Long roleId;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean mfaEnabled = false;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
