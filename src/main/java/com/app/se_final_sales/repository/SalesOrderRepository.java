package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByPartnerCompanyId(Long companyId);
}
