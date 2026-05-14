package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceInvoiceId(Long invoiceId);
    List<Payment> findByInvoicePartnerCompanyId(Long companyId);
}
