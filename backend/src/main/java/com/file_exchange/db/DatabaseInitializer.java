package com.file_exchange.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private final String dbUrl = "jdbc:sqlite:users.db";

    public void initialize(){
        try(Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement()){
            // create shcheme users
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT, " +
                            "email TEXT UNIQUE, " +
                            "password TEXT)");

            //  create shcheme files
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS files (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "user_id INTEGER, " +
                            "file_name TEXT, " +
                            "file_path TEXT, " +
                            "size INTEGER, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
