package com.file_exchange.repository;


import com.file_exchange.dto.UserDto;

import java.sql.*;


public class UserRepository {
    private final Connection conn;

    public UserRepository() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public UserDto createUser(UserDto userDto, String password) {
        try(PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (name,email,password) VALUES (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)){
            stmt.setString(1,userDto.getName());
            stmt.setString(2,userDto.getEmail());
            stmt.setString(3,password);
            userDto.setPassword(password);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    userDto.setId(rs.getLong(1));
                }
            }

            userDto.setPassword(password);

            return userDto;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public UserDto findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,email);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
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
}
