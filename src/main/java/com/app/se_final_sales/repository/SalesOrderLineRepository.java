package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
}
