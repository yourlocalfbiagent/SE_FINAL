package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditEntry;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.ReportExport;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.AuditLogSpec;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.ReportExportRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies/{companyId}/reports")
public class ReportController {

    private final CompanyRepository companies;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AuditLogRepository auditRepo;
    private final ReportExportRepository reportExportRepo;

    public ReportController(CompanyRepository companies, UserRepository userRepo,
                            RoleRepository roleRepo, AuditLogRepository auditRepo,
                            ReportExportRepository reportExportRepo) {
        this.companies        = companies;
        this.userRepo         = userRepo;
        this.roleRepo         = roleRepo;
        this.auditRepo        = auditRepo;
        this.reportExportRepo = reportExportRepo;
    }

    @GetMapping("/admin-stats")
    public Map<String, Object> adminStats(@PathVariable int companyId, HttpServletRequest request) {
        ensureCompany(companyId);
        ensureSameCompany(companyId, request);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers",    userRepo.countByCompanyId(companyId));
        stats.put("activeUsers",   userRepo.countByCompanyIdAndIsActive(companyId, true));
        stats.put("inactiveUsers", userRepo.countByCompanyIdAndIsActive(companyId, false));
        stats.put("lockedUsers",   userRepo.countLocked(companyId));
        stats.put("totalRoles",    roleRepo.countByCompanyId(companyId));
        stats.put("activeRoles",   roleRepo.countByCompanyIdAndIsActive(companyId, true));
        stats.put("auditEntries",  auditRepo.countByCompanyId(companyId));
        stats.put("recentExports", reportExportRepo.countByCompanyId(companyId));
        return stats;
    }

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

        List<AuditEntry> entries = fetchAuditEntries(companyId, null, null, null, from, to, limit);

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

        reportExportRepo.save(new ReportExport(companyId, cu.userId(), "audit-log", from, to, "csv", entries.size()));

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/exports")
    public List<ReportExport> listExports(@PathVariable int companyId,
                                          @RequestParam(defaultValue = "50") int limit,
                                          HttpServletRequest request) {
        ensureCompany(companyId);
        ensureSameCompany(companyId, request);

        List<ReportExport> all = reportExportRepo.findByCompanyIdOrderByGeneratedAtDesc(companyId);
        List<ReportExport> limited = all.subList(0, Math.min(all.size(), Math.min(limit, 200)));

        Set<Integer> genByIds = limited.stream()
                .map(ReportExport::getGeneratedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!genByIds.isEmpty()) {
            Map<Integer, String> emailById = userRepo.findAllById(genByIds).stream()
                    .collect(Collectors.toMap(User::getUserId, User::getEmail));
            limited.forEach(re -> re.setGeneratedByEmail(emailById.get(re.getGeneratedBy())));
        }
        return limited;
    }

    private List<AuditEntry> fetchAuditEntries(int companyId, Integer userId, String action,
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
