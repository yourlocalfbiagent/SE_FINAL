package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "journal_entry_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    @Column(name = "journal_entry_id", nullable = false)
    private Long journalEntryId;

    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Builder.Default
    @Column(name = "debit_amount", precision = 19, scale = 4)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "credit_amount", precision = 19, scale = 4)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "line_description", length = 500)
    private String lineDescription;
}
