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

    public long getPersonId(String email) {
        String query = "SELECT idPerson FROM person WHERE email = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("idPerson");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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

            // Check if `person` table exists and whether it already has AUTOINCREMENT
            String tableSql = null;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name='person'")) {
                if (rs.next()) {
                    tableSql = rs.getString("sql");
                }
            }

            if (tableSql == null) {
                // table doesn't exist -> create it with AUTOINCREMENT
                stmt.execute("CREATE TABLE IF NOT EXISTS person (" +
                        "idPerson INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "surname TEXT NOT NULL," +
                        "email TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "dateOfBirth TEXT NOT NULL" +
                        ");");

                // set AUTOINCREMENT start so next id will be 1000, but don't decrease existing
                // sequence
                try {
                    try (ResultSet rsSeq = stmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='person'")) {
                        if (rsSeq.next()) {
                            int seq = rsSeq.getInt("seq");
                            if (seq < 99999) {
                                stmt.execute("UPDATE sqlite_sequence SET seq = 99999 WHERE name='person'");
                            }
                        } else {
                            // sqlite_sequence exists but no entry for person
                            stmt.execute("INSERT INTO sqlite_sequence(name, seq) VALUES('person', 99999);");
                        }
                    }
                } catch (SQLException e) {
                    // sqlite_sequence may not exist yet or permission issues; don't fail
                    // initialization
                    System.err.println("Could not set AUTOINCREMENT start for person: " + e.getMessage());
                }

            } else if (!tableSql.toUpperCase().contains("AUTOINCREMENT")) {
                // table exists but without AUTOINCREMENT -> migrate safely
                System.out.println("Migrating existing person table to add AUTOINCREMENT primary key...");
                try {
                    conn.setAutoCommit(false);
                    // disable foreign keys during migration
                    stmt.execute("PRAGMA foreign_keys = OFF;");

                    // create new table with AUTOINCREMENT
                    stmt.execute("CREATE TABLE IF NOT EXISTS person_new (" +
                            "idPerson INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "surname TEXT NOT NULL," +
                            "email TEXT NOT NULL," +
                            "phone TEXT NOT NULL," +
                            "dateOfBirth TEXT NOT NULL" +
                            ");");

                    // copy data (preserve existing ids)
                    stmt.execute("INSERT INTO person_new (idPerson, name, surname, email, phone, dateOfBirth) " +
                            "SELECT idPerson, name, surname, email, phone, dateOfBirth FROM person;");

                    // drop old table and rename new
                    stmt.execute("DROP TABLE person;");
                    stmt.execute("ALTER TABLE person_new RENAME TO person;");

                    // re-enable foreign keys
                    stmt.execute("PRAGMA foreign_keys = ON;");

                    // set sqlite_sequence so next id >= 1000 (don't lower existing seq)
                    try {
                        try (ResultSet rsSeq = stmt
                                .executeQuery("SELECT seq FROM sqlite_sequence WHERE name='person'")) {
                            if (rsSeq.next()) {
                                int seq = rsSeq.getInt("seq");
                                if (seq < 999) {
                                    stmt.execute("UPDATE sqlite_sequence SET seq = 999 WHERE name='person'");
                                }
                            } else {
                                stmt.execute("INSERT INTO sqlite_sequence(name, seq) VALUES('person', 999);");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println(
                                "Could not set AUTOINCREMENT start for person after migration: " + e.getMessage());
                    }

                    conn.commit();
                    conn.setAutoCommit(true);
                    System.out.println("Migration complete.");
                } catch (SQLException ex) {
                    try {
                        conn.rollback();
                    } catch (SQLException rbe) {
                        System.err.println("Rollback failed: " + rbe.getMessage());
                    }
                    System.err.println("Error migrating person table: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                // table exists and already has AUTOINCREMENT -> ensure sequence starts at 999
                // (don't lower current value)
                try {
                    try (ResultSet rsSeq = stmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='person'")) {
                        if (rsSeq.next()) {
                            int seq = rsSeq.getInt("seq");
                            if (seq < 999) {
                                stmt.execute("UPDATE sqlite_sequence SET seq = 999 WHERE name='person'");
                            }
                        } else {
                            stmt.execute("INSERT INTO sqlite_sequence(name, seq) VALUES('person', 999);");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Could not set AUTOINCREMENT start for person: " + e.getMessage());
                }
            }

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
                    "pathDoc TEXT NOT NULL," +
                    "language char(2) NOT NULL," +
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

            // Admin Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS admin_settings (" +
                    "key TEXT PRIMARY KEY," +
                    "value TEXT NOT NULL" +
                    ");");

            System.out.println("Database initialized with full schema.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean recordAttendance(long personId) {
        // First check if person exists
        String checkPersonSql = "SELECT 1 FROM person WHERE idPerson = ?";
        String insertAttendanceSql = "INSERT INTO attendance (idPerson, atendanceDay, attendanceStart) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkPersonSql)) {

            checkStmt.setLong(1, personId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Person with ID " + personId + " not found.");
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertAttendanceSql)) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                insertStmt.setLong(1, personId);
                insertStmt.setString(2, now.toLocalDate().toString());
                insertStmt.setString(3, now.toLocalTime().toString());

                int affectedRows = insertStmt.executeUpdate();
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error recording attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getDocumentPath(String language) {
        String query = "SELECT pathDoc FROM document WHERE language = ? ORDER BY idDocument DESC LIMIT 1";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, language);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pathDoc");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertDocument(String name, String path, String language, String validFrom) {
        String query = "INSERT INTO document (nameDoc, pathDoc, language, validityFrom) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, path);
            pstmt.setString(3, language);
            pstmt.setString(4, validFrom);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getAdminPassword() {
        String query = "SELECT value FROM admin_settings WHERE key = 'admin_password'";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "admin"; // Default password
    }

    public boolean setAdminPassword(String newPassword) {
        String query = "INSERT OR REPLACE INTO admin_settings (key, value) VALUES ('admin_password', ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<org.example.model.Person> searchPersons(String queryStr) {
        java.util.List<org.example.model.Person> persons = new java.util.ArrayList<>();
        String sql = "SELECT * FROM person WHERE name LIKE ? OR surname LIKE ? OR email LIKE ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + queryStr + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    org.example.model.Person person = new org.example.model.Person(
                            rs.getLong("idPerson"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("dateOfBirth"));
                    persons.add(person);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return persons;
    }

    public boolean updatePerson(org.example.model.Person person) {
        String sql = "UPDATE person SET name = ?, surname = ?, email = ?, phone = ?, dateOfBirth = ? WHERE idPerson = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, person.getName());
            pstmt.setString(2, person.getSurname());
            pstmt.setString(3, person.getEmail());
            pstmt.setString(4, person.getPhone());
            pstmt.setString(5, person.getDateOfBirth());
            pstmt.setLong(6, person.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        new DatabaseManager().initialize();
    }
}
