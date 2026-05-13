package com.app.se_final_sales.repository;

import com.app.se_final_sales.entity.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {
}
