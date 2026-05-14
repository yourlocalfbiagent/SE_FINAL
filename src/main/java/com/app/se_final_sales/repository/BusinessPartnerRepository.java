package com.app.se_final_sales.repository;

import com.sefinal.erp.entity.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {
    List<BusinessPartner> findByCompanyId(Long companyId);
    List<BusinessPartner> findByIsActiveTrue();
}
