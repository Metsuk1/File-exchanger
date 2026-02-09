package com.file_exchange.repository;

import com.file_exchange.entity.SharedLink;
import java.sql.*;
import javax.sql.DataSource;

public class SharedLinkRepository {
    private final DataSource dataSource;

    public SharedLinkRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(SharedLink link) {
        String sql = "INSERT INTO shared_links (file_id, token, created_at) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, link.getFileId());
            stmt.setString(2, link.getToken());
            stmt.setString(3, link.getCreatedAt());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    link.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save shared link", e);
        }
    }

    public SharedLink findByToken(String token) {
        String sql = "SELECT * FROM shared_links WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SharedLink link = new SharedLink();
                    link.setId(rs.getLong("id"));
                    link.setFileId(rs.getLong("file_id"));
                    link.setToken(rs.getString("token"));
                    link.setCreatedAt(rs.getString("created_at"));
                    return link;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find shared link by token", e);
        }
    }

    public SharedLink findByFileId(Long fileId) {
        String sql = "SELECT * FROM shared_links WHERE file_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, fileId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SharedLink link = new SharedLink();
                    link.setId(rs.getLong("id"));
                    link.setFileId(rs.getLong("file_id"));
                    link.setToken(rs.getString("token"));
                    link.setCreatedAt(rs.getString("created_at"));
                    return link;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find shared link by file ID", e);
        }
    }

    public void deleteByFileId(Long fileId) {
        String sql = "DELETE FROM shared_links WHERE file_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, fileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete shared link", e);
        }
    }
}
