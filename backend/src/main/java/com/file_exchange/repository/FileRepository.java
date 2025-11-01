package com.file_exchange.repository;

import java.sql.*;
import java.util.ArrayList;
import com.file_exchange.entity.File;
import java.util.List;

public class FileRepository {
    private final Connection conn;

    public FileRepository() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public Long saveFile(File file) {
        String sql = "INSERT INTO files (user_id, file_name, file_path, size) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            stmt.setLong(1, file.getUserId());
            stmt.setString(2, file.getFileName());
            stmt.setString(3, file.getFilePath());
            stmt.setLong(4, file.getSize());
            stmt.executeUpdate();

            try(ResultSet rs = stmt.getGeneratedKeys()) {
                if(rs.next()) {
                    file.setId(rs.getLong(1));
                }else{
                    throw new SQLException("Failed to get generated file ID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save file", e);
        }

        return file.getId(); // to change,maybe return null ?
    }

    public List<File> getUserFiles(Long userId) {
        List<File> files = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM files WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    File file = new File();
                    file.setId(rs.getLong("id"));
                    file.setUserId(rs.getLong("user_id"));
                    file.setFileName(rs.getString("file_name"));
                    file.setFilePath(rs.getString("file_path"));
                    file.setSize(rs.getLong("size"));
                    files.add(file); // To change
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user files", e);
        }
        return files;
    }


    public File getFileById(Long fileId, Long userId) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM files WHERE id = ? AND user_id = ?")) {
            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    File file = new File();
                    file.setId(rs.getLong("id"));
                    file.setUserId(rs.getLong("user_id"));
                    file.setFileName(rs.getString("file_name"));
                    file.setFilePath(rs.getString("file_path"));
                    file.setSize(rs.getLong("size"));
                    return file;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get file", e);
        }
    }

    public void deleteFile(Long fileId, Long userId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM files WHERE id = ? AND user_id = ?")) {
            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
