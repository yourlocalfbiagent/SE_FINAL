package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final DataSource ds;

    public UserDao(DataSource ds) {
        this.ds = ds;
    }

    public User create(User u) {
        String sql = """
                INSERT INTO users (
                    first_name, last_name, email, password_hash,
                    company_id, role_id, is_active, mfa_enabled
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.firstName());
            ps.setString(2, u.lastName());
            ps.setString(3, u.email());
            ps.setString(4, u.passwordHash());
            ps.setInt(5, u.companyId());
            if (u.roleId() != null) ps.setInt(6, u.roleId()); else ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, u.isActive());
            ps.setBoolean(8, u.mfaEnabled());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getInt(1)).orElseThrow();
            }
            throw new SQLException("No generated key returned for users insert");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    public Optional<User> findById(int id) {
        return queryOne("SELECT * FROM users WHERE user_id = ?", id);
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by email " + email, e);
        }
    }

    public List<User> findByCompany(int companyId) {
        String sql = "SELECT * FROM users WHERE company_id = ? ORDER BY user_id";
        List<User> out = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list users for company " + companyId, e);
        }
    }

    public void setActive(int userId, boolean active) {
        execUpdate("UPDATE users SET is_active = ? WHERE user_id = ?", ps -> {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
        });
    }

    public void updatePassword(int userId, String newHash) {
        execUpdate("UPDATE users SET password_hash = ?, failed_login_attempts = 0, locked_until = NULL WHERE user_id = ?",
                ps -> { ps.setString(1, newHash); ps.setInt(2, userId); });
    }

    /** NFR-10: returns the new attempt count after the increment. */
    public int recordFailedLogin(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE user_id = ? RETURNING failed_login_attempts";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new RuntimeException("user " + userId + " not found while recording failed login");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record failed login for user " + userId, e);
        }
    }

    public void lockUntil(int userId, java.time.LocalDateTime until) {
        execUpdate("UPDATE users SET locked_until = ? WHERE user_id = ?", ps -> {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(until));
            ps.setInt(2, userId);
        });
    }

    public void clearLoginCounters(int userId) {
        execUpdate("UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE user_id = ?",
                ps -> ps.setInt(1, userId));
    }

    @FunctionalInterface
    private interface PsBinder { void bind(PreparedStatement ps) throws SQLException; }

    private void execUpdate(String sql, PsBinder binder) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + sql, e);
        }
    }

    private Optional<User> queryOne(String sql, int idParam) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idParam);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Timestamp lockedUntil = rs.getTimestamp("locked_until");
        int role = rs.getInt("role_id");
        Integer roleId = rs.wasNull() ? null : role;
        return new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("company_id"),
                roleId,
                rs.getBoolean("is_active"),
                rs.getBoolean("mfa_enabled"),
                rs.getInt("failed_login_attempts"),
                lockedUntil != null ? lockedUntil.toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
