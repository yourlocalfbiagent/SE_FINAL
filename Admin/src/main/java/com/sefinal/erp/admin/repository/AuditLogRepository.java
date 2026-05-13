package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
                                             JpaSpecificationExecutor<AuditLog> {

    long countByCompanyId(int companyId);
}
