package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.model.AuditEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-log")
public class AuditController {

    private final AuditDao audit;

    public AuditController(AuditDao audit) { this.audit = audit; }

    /** Audit log is scoped to the caller's company — tenants cannot see each other's history. */
    @GetMapping
    public List<AuditEntry> recent(HttpServletRequest request,
                                   @RequestParam(defaultValue = "100") int limit) {
        CurrentUser cu = AuthInterceptor.current(request);
        int capped = Math.min(Math.max(limit, 1), 500);
        return audit.findRecent(cu.companyId(), capped);
    }
}
