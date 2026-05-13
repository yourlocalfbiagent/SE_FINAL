package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.ReportExport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportExportRepository extends JpaRepository<ReportExport, Integer> {

    List<ReportExport> findByCompanyIdOrderByGeneratedAtDesc(int companyId);

    long countByCompanyId(int companyId);
}
