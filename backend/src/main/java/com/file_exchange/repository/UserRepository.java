package com.file_exchange.repository;

import com.file_exchange.dto.UserDto;
import java.sql.*;

/**
 * Database operations for User entity
 */
public class UserRepository {
    private final Connection conn;

    public UserRepository() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public boolean existsByEmail(String email) {
        return findUserByEmail(email) != null;
    }

    public UserDto save(UserDto userDto, String hashedPassword) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (name,email,password) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, userDto.getName());
            stmt.setString(2, userDto.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    userDto.setId(rs.getLong(1));
                }
            }

            return userDto;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public UserDto findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDto userDto = new UserDto();
                    userDto.setId(rs.getLong("id"));
                    userDto.setName(rs.getString("name"));
                    userDto.setEmail(rs.getString("email"));
                    userDto.setPassword(rs.getString("password"));

                    return userDto;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }

        return null;
    }

    public UserDto findById(Long id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDto userDto = new UserDto();
                    userDto.setId(rs.getLong("id"));
                    userDto.setName(rs.getString("name"));
                    userDto.setEmail(rs.getString("email"));
                    return userDto;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return null;
    }

    public void updateUser(Long id, String name, String email) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setLong(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public boolean existsByEmailAndNotId(String email, Long id) {
        String sql = "SELECT id FROM users WHERE email = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setLong(2, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email uniqueness", e);
        }
    }
}
