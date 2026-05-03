package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.Role;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDao {

    private final DataSource ds;

    public RoleDao(DataSource ds) {
        this.ds = ds;
    }

    public Role create(Role r) {
        String sql = """
                INSERT INTO roles (role_name, description, company_id, is_active)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.roleName());
            ps.setString(2, r.description());
            ps.setInt(3, r.companyId());
            ps.setBoolean(4, r.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getInt(1)).orElseThrow();
            }
            throw new SQLException("No generated key returned for roles insert");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert role", e);
        }
    }

    public Optional<Role> findById(int id) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load role " + id, e);
        }
    }

    public List<Role> findByCompany(int companyId) {
        String sql = "SELECT * FROM roles WHERE company_id = ? ORDER BY role_id";
        List<Role> out = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list roles for company " + companyId, e);
        }
    }

    public void grantPermission(int roleId, int permissionId) {
        String sql = """
                INSERT INTO role_permissions (role_id, permission_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to grant permission " + permissionId + " to role " + roleId, e);
        }
    }

    public void revokePermission(int roleId, int permissionId) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to revoke permission " + permissionId + " from role " + roleId, e);
        }
    }

    private Role map(ResultSet rs) throws SQLException {
        return new Role(
                rs.getInt("role_id"),
                rs.getString("role_name"),
                rs.getString("description"),
                rs.getInt("company_id"),
                rs.getBoolean("is_active")
        );
    }
}
