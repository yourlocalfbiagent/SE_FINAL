package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.AuditEntry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditDao {

    private final DataSource ds;

    public AuditDao(DataSource ds) { this.ds = ds; }

    public void log(Integer userId, int companyId, String action,
                    String entityType, Integer entityId, String details) {
        String sql = """
                INSERT INTO audit_log (user_id, company_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, companyId);
            ps.setString(3, action);
            ps.setString(4, entityType);
            if (entityId != null) ps.setInt(5, entityId); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write audit entry", e);
        }
    }

    /** FR-18: filtered query supporting optional userId, action prefix, entityType, and date range. */
    public List<AuditEntry> findFiltered(int companyId, Integer userId, String action,
                                         String entityType, LocalDate from, LocalDate to, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT al.audit_id, al.user_id, u.email AS user_email, al.company_id, al.action,
                       al.entity_type, al.entity_id, al.details, al.created_at
                FROM audit_log al
                LEFT JOIN users u ON u.user_id = al.user_id
                WHERE al.company_id = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(companyId);
        if (userId != null)     { sql.append(" AND al.user_id = ?");          params.add(userId); }
        if (action != null && !action.isBlank())     { sql.append(" AND al.action LIKE ?"); params.add(action + "%"); }
        if (entityType != null && !entityType.isBlank()) { sql.append(" AND al.entity_type = ?"); params.add(entityType); }
        if (from != null) { sql.append(" AND al.created_at >= ?"); params.add(Timestamp.valueOf(from.atStartOfDay())); }
        if (to   != null) { sql.append(" AND al.created_at <  ?"); params.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay())); }
        sql.append(" ORDER BY al.created_at DESC LIMIT ?");
        params.add(Math.min(Math.max(limit, 1), 500));

        List<AuditEntry> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(mapRow(rs)); }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query audit log", e);
        }
    }

    private AuditEntry mapRow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        Integer userIdBoxed = rs.wasNull() ? null : userId;
        int entityId = rs.getInt("entity_id");
        Integer entityIdBoxed = rs.wasNull() ? null : entityId;
        return new AuditEntry(
                rs.getLong("audit_id"),
                userIdBoxed,
                rs.getString("user_email"),
                rs.getInt("company_id"),
                rs.getString("action"),
                rs.getString("entity_type"),
                entityIdBoxed,
                rs.getString("details"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
