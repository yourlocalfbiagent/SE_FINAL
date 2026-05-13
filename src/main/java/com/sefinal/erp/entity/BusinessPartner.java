package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUSINESS_PARTNERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partnerId;

    @Column(nullable = false)
    private String partnerName;

    @Column(unique = true)
    private String email;

    @Column(length = 50)
    private String phone;

    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 50)
    private String type;

    private Long companyId;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
