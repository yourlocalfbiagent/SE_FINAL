package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.AuditEntry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

    public List<AuditEntry> findRecent(int companyId, int limit) {
        String sql = """
                SELECT al.audit_id, al.user_id, u.email AS user_email, al.company_id, al.action,
                       al.entity_type, al.entity_id, al.details, al.created_at
                FROM audit_log al
                LEFT JOIN users u ON u.user_id = al.user_id
                WHERE al.company_id = ?
                ORDER BY al.created_at DESC
                LIMIT ?
                """;
        List<AuditEntry> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    Integer userIdBoxed = rs.wasNull() ? null : userId;
                    int entityId = rs.getInt("entity_id");
                    Integer entityIdBoxed = rs.wasNull() ? null : entityId;
                    out.add(new AuditEntry(
                            rs.getLong("audit_id"),
                            userIdBoxed,
                            rs.getString("user_email"),
                            rs.getInt("company_id"),
                            rs.getString("action"),
                            rs.getString("entity_type"),
                            entityIdBoxed,
                            rs.getString("details"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load audit log", e);
        }
    }
}
