package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reminders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id")
    private Long reminderId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "sent_by")
    private Long sentBy;

    @Column(name = "reminder_type", nullable = false, length = 30)
    private String reminderType;

    @Column(nullable = false, length = 30)
    private String channel;

    @Column(name = "due_date_snapshot")
    private LocalDate dueDateSnapshot;

    @Column(name = "sent_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}
