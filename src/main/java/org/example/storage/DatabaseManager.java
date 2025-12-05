package org.example.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:database.db";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public boolean isEmailExists(String email) {
        String query = "SELECT 1 FROM person WHERE email = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerPerson(String name, String surname, String email, String phone,
            java.time.LocalDate dateOfBirth) {
        String query = "INSERT INTO person (name, surname, email, phone, dateOfBirth) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, dateOfBirth.toString());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error registering person: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void initialize() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Enable Foreign Keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Person Table
            stmt.execute("CREATE TABLE IF NOT EXISTS person (" +
                    "idPerson INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "surname TEXT NOT NULL," +
                    "email TEXT NOT NULL," +
                    "phone TEXT NOT NULL," +
                    "dateOfBirth TEXT NOT NULL" +
                    ");");

            // Attendance Table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "idAtendance INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "idPerson INTEGER NOT NULL," +
                    "atendanceDay TEXT NOT NULL," +
                    "attendanceStart TEXT NOT NULL," +
                    "attendanceEnd TEXT," +
                    "FOREIGN KEY (idPerson) REFERENCES person(idPerson)" +
                    ");");

            // Contact Type Table
            stmt.execute("CREATE TABLE IF NOT EXISTS \"typ kontaktu\" (" +
                    "\"idtyp kontaktu\" INTEGER PRIMARY KEY," +
                    "typKontaktu INTEGER," +
                    "kontakt TEXT" +
                    ");");

            // Position Table
            stmt.execute("CREATE TABLE IF NOT EXISTS position (" +
                    "idPosition INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "positionName TEXT NOT NULL" +
                    ");");

            // Person Has Position Table
            stmt.execute("CREATE TABLE IF NOT EXISTS personHasPossition (" +
                    "idPositionStamp INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "idPerson INTEGER NOT NULL," +
                    "idPosition INTEGER NOT NULL," +
                    "positionFrom TEXT," +
                    "positionTo TEXT," +
                    "FOREIGN KEY (idPerson) REFERENCES person(idPerson)," +
                    "FOREIGN KEY (idPosition) REFERENCES position(idPosition)" +
                    ");");

            // Permission Table
            stmt.execute("CREATE TABLE IF NOT EXISTS permission (" +
                    "idPermission INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "permName TEXT NOT NULL" +
                    ");");

            // Person Has Permission Table
            stmt.execute("CREATE TABLE IF NOT EXISTS personHasPermission (" +
                    "idPersonPerm INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "idPerson INTEGER NOT NULL," +
                    "idPermission INTEGER NOT NULL," +
                    "permFrom TEXT," +
                    "permTo TEXT," +
                    "FOREIGN KEY (idPerson) REFERENCES person(idPerson)," +
                    "FOREIGN KEY (idPermission) REFERENCES permission(idPermission)" +
                    ");");

            // Document Table
            stmt.execute("CREATE TABLE IF NOT EXISTS document (" +
                    "idDocument INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nameDoc TEXT NOT NULL," +
                    "textDoc TEXT NOT NULL," +
                    "validityFrom TEXT NOT NULL," +
                    "validityTo TEXT" +
                    ");");

            // Person Has Document Table
            stmt.execute("CREATE TABLE IF NOT EXISTS personHasDocument (" +
                    "idDocument INTEGER NOT NULL," +
                    "idPerson INTEGER NOT NULL," +
                    "dateOfSign TEXT," +
                    "cancelationOfSign TEXT," +
                    "PRIMARY KEY (idDocument, idPerson)," +
                    "FOREIGN KEY (idDocument) REFERENCES document(idDocument)," +
                    "FOREIGN KEY (idPerson) REFERENCES person(idPerson)" +
                    ");");

            // Log Table
            stmt.execute("CREATE TABLE IF NOT EXISTS log (" +
                    "idLog INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "logWhatsGoingOn TEXT NOT NULL," +
                    "logDateTime TEXT NOT NULL" +
                    ");");

            System.out.println("Database initialized with full schema.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DatabaseManager().initialize();
    }
}
