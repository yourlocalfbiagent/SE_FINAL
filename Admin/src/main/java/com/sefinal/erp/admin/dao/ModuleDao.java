package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.Module;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModuleDao {

    private final DataSource ds;

    public ModuleDao(DataSource ds) { this.ds = ds; }

    public List<Module> findAll() {
        String sql = "SELECT module_id, module_name, is_active FROM modules ORDER BY module_id";
        List<Module> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Module(
                        rs.getInt("module_id"),
                        rs.getString("module_name"),
                        rs.getBoolean("is_active")));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list modules", e);
        }
    }
}
