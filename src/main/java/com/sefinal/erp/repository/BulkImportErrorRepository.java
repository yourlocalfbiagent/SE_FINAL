package com.sefinal.erp.repository;

import com.sefinal.erp.entity.BulkImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkImportErrorRepository extends JpaRepository<BulkImportError, Long> {
    List<BulkImportError> findByImportId(Long importId);
}
