package org.example.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:database.db";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void initialize() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Create tables if they do not exist
            // Example table for verification
            String sql = "CREATE TABLE IF NOT EXISTS test_table (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL" +
                    ");";
            stmt.execute(sql);

            System.out.println("Database initialized.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DatabaseManager().initialize();
    }
}
