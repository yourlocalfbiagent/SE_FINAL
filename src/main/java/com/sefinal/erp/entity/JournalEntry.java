package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "posted_by")
    private Long postedBy;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "reference_number", unique = true, nullable = false, length = 50)
    private String referenceNumber;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "posted_at")
    private LocalDateTime postedAt;
}
