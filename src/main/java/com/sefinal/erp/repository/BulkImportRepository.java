package com.sefinal.erp.repository;

import com.sefinal.erp.entity.BulkImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkImportRepository extends JpaRepository<BulkImport, Long> {
    List<BulkImport> findByCompanyId(Long companyId);
}
