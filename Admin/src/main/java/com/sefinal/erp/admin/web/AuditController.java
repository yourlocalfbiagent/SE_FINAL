package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditEntry;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.AuditLogSpec;
import com.sefinal.erp.admin.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-log")
public class AuditController {

    private final AuditLogRepository auditRepo;
    private final UserRepository userRepo;

    public AuditController(AuditLogRepository auditRepo, UserRepository userRepo) {
        this.auditRepo = auditRepo;
        this.userRepo  = userRepo;
    }

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
        return fetchFiltered(cu.companyId(), userId, action, entityType, from, to, limit);
    }

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
        List<AuditEntry> entries = fetchFiltered(cu.companyId(), userId, action, entityType, from, to, limit);

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

    private List<AuditEntry> fetchFiltered(int companyId, Integer userId, String action,
                                            String entityType, LocalDate from, LocalDate to, int limit) {
        var spec = AuditLogSpec.filter(companyId, userId, action, entityType, from, to);
        var page = PageRequest.of(0, Math.min(Math.max(limit, 1), 500), Sort.by("createdAt").descending());
        List<AuditLog> logs = auditRepo.findAll(spec, page).getContent();

        Set<Integer> uids = logs.stream()
                .map(AuditLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> emailById = uids.isEmpty() ? Map.of() :
                userRepo.findAllById(uids).stream()
                        .collect(Collectors.toMap(User::getUserId, User::getEmail));

        return logs.stream().map(al -> new AuditEntry(
                al.getAuditId(), al.getUserId(), emailById.get(al.getUserId()),
                al.getCompanyId(), al.getAction(), al.getEntityType(),
                al.getEntityId(), al.getDetails(), al.getCreatedAt()
        )).toList();
    }
}
