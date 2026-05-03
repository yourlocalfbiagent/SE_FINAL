package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.model.AuditEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audit-log")
public class AuditController {

    private final AuditDao audit;

    public AuditController(AuditDao audit) { this.audit = audit; }

    /** FR-18: filtered audit log scoped to caller's company. */
    @GetMapping
    public List<AuditEntry> query(
            HttpServletRequest request,
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CurrentUser cu = AuthInterceptor.current(request);
        return audit.findFiltered(cu.companyId(), userId, action, entityType, from, to, limit);
    }

    /** FR-20: export audit log as CSV for the caller's company. */
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            HttpServletRequest request,
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CurrentUser cu = AuthInterceptor.current(request);
        List<AuditEntry> entries = audit.findFiltered(cu.companyId(), userId, action, entityType, from, to, limit);

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

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
