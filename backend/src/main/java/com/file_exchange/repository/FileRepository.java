package com.file_exchange.repository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRepository {
    private final Connection conn;

    public FileRepository() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public void saveFile(Long userId, String fileName, String filePath, long size) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO files (user_id, file_name, file_path, size) VALUES (?, ?, ?, ?)")) {
            stmt.setLong(1, userId);
            stmt.setString(2, fileName);
            stmt.setString(3, filePath);
            stmt.setLong(4, size);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    public List<Map<String, Object>> getUserFiles(Long userId) {
        List<Map<String, Object>> files = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM files WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("id", rs.getLong("id"));
                    file.put("file_name", rs.getString("file_name"));
                    file.put("file_path", rs.getString("file_path"));
                    file.put("size", rs.getLong("size"));
                    files.add(file);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user files", e);
        }
        return files;
    }

    public String getFilePath(Long fileId, Long userId) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT file_path FROM files WHERE id = ? AND user_id = ?")) {
            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_path");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get file path", e);
        }
    }

    public InputStream getUserFile(Long userId, Long fileId) {
        String filePath = getFilePath(fileId, userId);
        if (filePath != null) {
            try {
                return new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found", e);
            }
        }
        throw new IllegalArgumentException("File not found for user " + userId);
    }
}
