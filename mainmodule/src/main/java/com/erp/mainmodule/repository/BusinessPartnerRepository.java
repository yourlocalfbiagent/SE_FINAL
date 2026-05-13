package com.erp.mainmodule.repository;

import com.erp.mainmodule.entity.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Integer> {
}