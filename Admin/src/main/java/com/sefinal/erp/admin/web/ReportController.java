package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.dao.ReportDao;
import com.sefinal.erp.admin.model.AuditEntry;
import com.sefinal.erp.admin.model.ReportExport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/reports")
public class ReportController {

    private final ReportDao reports;
    private final AuditDao audit;
    private final CompanyDao companies;

    public ReportController(ReportDao reports, AuditDao audit, CompanyDao companies) {
        this.reports = reports;
        this.audit = audit;
        this.companies = companies;
    }

    /** FR-19: admin stats summary (users, roles, audit counts). */
    @GetMapping("/admin-stats")
    public Map<String, Object> adminStats(@PathVariable int companyId, HttpServletRequest request) {
        ensureCompany(companyId);
        ensureSameCompany(companyId, request);
        return reports.adminStats(companyId);
    }

    /** FR-20: export audit log as CSV and record the export. */
    @GetMapping(value = "/audit-export", produces = "text/csv")
    public ResponseEntity<byte[]> auditExport(
            @PathVariable int companyId,
            HttpServletRequest request,
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ensureCompany(companyId);
        ensureSameCompany(companyId, request);
        CurrentUser cu = AuthInterceptor.current(request);

        List<AuditEntry> entries = audit.findFiltered(companyId, null, null, null, from, to, limit);

        StringBuilder csv = new StringBuilder("audit_id,user_id,user_email,action,entity_type,entity_id,details,created_at\n");
        for (AuditEntry e : entries) {
            csv.append(e.auditId()).append(',')
               .append(e.userId() != null ? e.userId() : "").append(',')
               .append(CsvUtil.escape(e.userEmail())).append(',')
               .append(CsvUtil.escape(e.action())).append(',')
               .append(CsvUtil.escape(e.entityType())).append(',')
               .append(e.entityId() != null ? e.entityId() : "").append(',')
               .append(CsvUtil.escape(e.details())).append(',')
               .append(e.createdAt()).append('\n');
        }

        reports.logExport(companyId, cu.userId(), "audit-log", from, to, "csv", entries.size());

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    /** List past exports for this company. */
    @GetMapping("/exports")
    public List<ReportExport> listExports(@PathVariable int companyId,
                                          @RequestParam(defaultValue = "50") int limit,
                                          HttpServletRequest request) {
        ensureCompany(companyId);
        ensureSameCompany(companyId, request);
        return reports.findExports(companyId, limit);
    }

    private void ensureCompany(int companyId) {
        companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company " + companyId + " not found"));
    }

    private static void ensureSameCompany(int companyId, HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        if (cu.companyId() != companyId) {
            throw new BadRequestException("cannot access reports for another company");
        }
    }

}
