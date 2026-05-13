package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.SalesInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceLineRepository extends JpaRepository<SalesInvoiceLine, Long> {
}
