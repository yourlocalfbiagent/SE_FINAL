package com.app.se_final_sales.entity;

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
    private String type; // e.g., CUSTOMER, SUPPLIER

    private Long companyId; // References external COMPANIES

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
