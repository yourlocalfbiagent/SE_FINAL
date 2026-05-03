package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.ReportExport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    private final DataSource ds;

    public ReportDao(DataSource ds) { this.ds = ds; }

    /** FR-19: summary stats for the admin dashboard scoped to one company. */
    public Map<String, Object> adminStats(int companyId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (Connection c = ds.getConnection()) {
            stats.put("totalUsers",    queryInt(c, "SELECT COUNT(*) FROM users WHERE company_id = ?", companyId));
            stats.put("activeUsers",   queryInt(c, "SELECT COUNT(*) FROM users WHERE company_id = ? AND is_active = true", companyId));
            stats.put("inactiveUsers", queryInt(c, "SELECT COUNT(*) FROM users WHERE company_id = ? AND is_active = false", companyId));
            stats.put("lockedUsers",   queryInt(c, "SELECT COUNT(*) FROM users WHERE company_id = ? AND locked_until > NOW()", companyId));
            stats.put("totalRoles",    queryInt(c, "SELECT COUNT(*) FROM roles WHERE company_id = ?", companyId));
            stats.put("activeRoles",   queryInt(c, "SELECT COUNT(*) FROM roles WHERE company_id = ? AND is_active = true", companyId));
            stats.put("auditEntries",  queryInt(c, "SELECT COUNT(*) FROM audit_log WHERE company_id = ?", companyId));
            stats.put("recentExports", queryInt(c, "SELECT COUNT(*) FROM report_exports WHERE company_id = ?", companyId));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute admin stats", e);
        }
        return stats;
    }

    /** FR-20: log a completed export to report_exports for traceability. */
    public ReportExport logExport(int companyId, Integer generatedBy, String reportType,
                                  LocalDate periodStart, LocalDate periodEnd,
                                  String fileFormat, int rowCount) {
        String sql = """
                INSERT INTO report_exports (company_id, generated_by, report_type, period_start, period_end, file_format, row_count)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING export_id, generated_at
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            if (generatedBy != null) ps.setInt(2, generatedBy); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, reportType);
            ps.setDate(4, periodStart != null ? Date.valueOf(periodStart) : null);
            ps.setDate(5, periodEnd   != null ? Date.valueOf(periodEnd)   : null);
            ps.setString(6, fileFormat);
            ps.setInt(7, rowCount);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new ReportExport(rs.getInt("export_id"), companyId, generatedBy, null,
                        reportType, periodStart, periodEnd, fileFormat, rowCount,
                        rs.getTimestamp("generated_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to log report export", e);
        }
    }

    /** List past exports for a company, newest first. */
    public List<ReportExport> findExports(int companyId, int limit) {
        String sql = """
                SELECT re.export_id, re.company_id, re.generated_by, u.email AS generated_by_email,
                       re.report_type, re.period_start, re.period_end, re.file_format, re.row_count, re.generated_at
                FROM report_exports re
                LEFT JOIN users u ON u.user_id = re.generated_by
                WHERE re.company_id = ?
                ORDER BY re.generated_at DESC
                LIMIT ?
                """;
        List<ReportExport> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            ps.setInt(2, Math.min(limit, 200));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int genBy = rs.getInt("generated_by");
                    Integer genByBoxed = rs.wasNull() ? null : genBy;
                    Date ps_ = rs.getDate("period_start");
                    Date pe_ = rs.getDate("period_end");
                    out.add(new ReportExport(
                            rs.getInt("export_id"), rs.getInt("company_id"),
                            genByBoxed, rs.getString("generated_by_email"),
                            rs.getString("report_type"),
                            ps_ != null ? ps_.toLocalDate() : null,
                            pe_ != null ? pe_.toLocalDate() : null,
                            rs.getString("file_format"),
                            rs.getObject("row_count", Integer.class),
                            rs.getTimestamp("generated_at").toLocalDateTime()));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list exports", e);
        }
    }

    private int queryInt(Connection c, String sql, int param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
}
