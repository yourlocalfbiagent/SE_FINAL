package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {
    List<SalesReturn> findByInvoicePartnerCompanyId(Long companyId);
}
