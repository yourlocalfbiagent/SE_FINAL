package com.sefinal.erp.admin.dao;

import com.sefinal.erp.admin.model.Action;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActionDao {

    private final DataSource ds;

    public ActionDao(DataSource ds) { this.ds = ds; }

    public List<Action> findAll() {
        String sql = "SELECT action_id, action_name FROM actions ORDER BY action_id";
        List<Action> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Action(rs.getInt("action_id"), rs.getString("action_name")));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list actions", e);
        }
    }
}
