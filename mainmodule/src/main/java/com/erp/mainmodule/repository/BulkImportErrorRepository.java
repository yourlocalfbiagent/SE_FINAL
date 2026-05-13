package com.erp.mainmodule.repository;

import com.erp.mainmodule.entity.BulkImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkImportErrorRepository extends JpaRepository<BulkImportError, Integer> {
}