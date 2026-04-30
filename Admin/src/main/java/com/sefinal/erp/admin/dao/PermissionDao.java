package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.Permission;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermissionDao {

    private final DataSource ds;

    public PermissionDao(DataSource ds) { this.ds = ds; }

    public List<Permission> findAll() {
        String sql = """
                SELECT p.permission_id, p.module_id, m.module_name, p.action_id, a.action_name
                FROM permissions p
                JOIN modules m ON m.module_id = p.module_id
                JOIN actions a ON a.action_id = p.action_id
                ORDER BY m.module_name, a.action_name
                """;
        List<Permission> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list permissions", e);
        }
    }

    public List<Permission> findForRole(int roleId) {
        String sql = """
                SELECT p.permission_id, p.module_id, m.module_name, p.action_id, a.action_name
                FROM role_permissions rp
                JOIN permissions p ON p.permission_id = rp.permission_id
                JOIN modules m     ON m.module_id     = p.module_id
                JOIN actions a     ON a.action_id     = p.action_id
                WHERE rp.role_id = ?
                ORDER BY m.module_name, a.action_name
                """;
        List<Permission> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list permissions for role " + roleId, e);
        }
    }

    private Permission map(ResultSet rs) throws SQLException {
        return new Permission(
                rs.getInt("permission_id"),
                rs.getInt("module_id"),
                rs.getString("module_name"),
                rs.getInt("action_id"),
                rs.getString("action_name"));
    }
}
