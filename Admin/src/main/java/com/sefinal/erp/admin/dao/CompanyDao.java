package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.Company;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyDao {

    private final DataSource ds;

    public CompanyDao(DataSource ds) {
        this.ds = ds;
    }

    public Company create(Company c) {
        String sql = """
                INSERT INTO companies (company_name, currency, tax_default, locale, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.companyName());
            ps.setString(2, c.currency());
            ps.setBigDecimal(3, c.taxDefault());
            ps.setString(4, c.locale());
            ps.setBoolean(5, c.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1)).orElseThrow();
                }
            }
            throw new SQLException("No generated key returned for companies insert");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert company", e);
        }
    }

    public Optional<Company> findById(int id) {
        String sql = "SELECT * FROM companies WHERE company_id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load company " + id, e);
        }
    }

    public Company update(Company c) {
        String sql = """
                UPDATE companies
                SET company_name = ?, currency = ?, tax_default = ?, locale = ?, is_active = ?
                WHERE company_id = ?
                """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.companyName());
            ps.setString(2, c.currency());
            ps.setBigDecimal(3, c.taxDefault());
            ps.setString(4, c.locale());
            ps.setBoolean(5, c.isActive());
            ps.setInt(6, c.companyId());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("company " + c.companyId() + " not found");
            return findById(c.companyId()).orElseThrow();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update company " + c.companyId(), e);
        }
    }

    public List<Company> findAll() {
        String sql = "SELECT * FROM companies ORDER BY company_id";
        List<Company> out = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list companies", e);
        }
    }

    private Company map(ResultSet rs) throws SQLException {
        return new Company(
                rs.getInt("company_id"),
                rs.getString("company_name"),
                rs.getString("currency"),
                rs.getBigDecimal("tax_default"),
                rs.getString("locale"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
