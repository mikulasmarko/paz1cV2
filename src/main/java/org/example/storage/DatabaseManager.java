package org.example.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

	private static final String URL = "jdbc:sqlite:database.db";

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL);
	}

	public boolean EmailExists(String email) {
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

	public boolean personExists(long personId) {
		String query = "SELECT 1 FROM person WHERE idPerson = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setLong(1, personId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<String> getPersonPositions(long personId) {
		List<String> positions = new ArrayList<>();
		String query = "SELECT pos.positionName " +
				"FROM personHasPossition php " +
				"JOIN position pos ON php.idPosition = pos.idPosition " +
				"WHERE php.idPerson = ? AND php.positionTo IS NULL";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setLong(1, personId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					positions.add(rs.getString("positionName"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return positions;
	}

	public boolean addPersonPosition(long personId, String positionName) {
		if (positionName == null || positionName.trim().isEmpty())
			return false;

		try (Connection conn = getConnection()) {
			// 1. Get Position ID
			int positionId = -1;
			String findPosSql = "SELECT idPosition FROM position WHERE positionName = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(findPosSql)) {
				pstmt.setString(1, positionName);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						positionId = rs.getInt("idPosition");
					}
				}
			}
			if (positionId == -1)
				return false; // Position must exist in predefined list

			// 2. Check if already assigned active
			String checkSql = "SELECT 1 FROM personHasPossition WHERE idPerson = ? AND idPosition = ? AND positionTo IS NULL";
			try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
				pstmt.setLong(1, personId);
				pstmt.setInt(2, positionId);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next())
						return true; // Already exists, consider success
				}
			}

			// 3. Insert new mapping
			String insertSql = "INSERT INTO personHasPossition (idPerson, idPosition, positionFrom) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
				pstmt.setLong(1, personId);
				pstmt.setInt(2, positionId);
				pstmt.setString(3, java.time.LocalDate.now().toString());
				return pstmt.executeUpdate() > 0;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean removePersonPosition(long personId, String positionName) {
		try (Connection conn = getConnection()) {
			// Get Position ID
			int positionId = -1;
			String findPosSql = "SELECT idPosition FROM position WHERE positionName = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(findPosSql)) {
				pstmt.setString(1, positionName);
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						positionId = rs.getInt("idPosition");
					}
				}
			}
			if (positionId == -1)
				return false;

			// Update positionTo = now
			String updateSql = "UPDATE personHasPossition SET positionTo = ? WHERE idPerson = ? AND idPosition = ? AND positionTo IS NULL";
			try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
				pstmt.setString(1, java.time.LocalDate.now().toString());
				pstmt.setLong(2, personId);
				pstmt.setInt(3, positionId);
				return pstmt.executeUpdate() > 0;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<String> getAllPositions() {
		List<String> positions = new ArrayList<>();
		String query = "SELECT positionName FROM position ORDER BY positionName";
		try (Connection conn = getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				positions.add(rs.getString("positionName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return positions;
	}

	public boolean addPosition(String positionName) {
		if (positionName == null || positionName.trim().isEmpty())
			return false;
		String query = "INSERT INTO position (positionName) VALUES (?)";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, positionName.trim());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deletePosition(String positionName) {
		// First check if any person has this position currently assigned?
		// Actually, let's just try to delete. If strict FKs are on, it might fail if
		// used.
		// But `personHasPossition` references `idPosition`.
		// We accept that we need to find ID first or delete by name.

		// Let's get ID first to be safe or delete by name where name matches.
		// "DELETE FROM position WHERE positionName = ?"

		String query = "DELETE FROM position WHERE positionName = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, positionName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			// Likely FK violation if used
			System.err.println("Error deleting position: " + e.getMessage());
			return false;
		}
	}

	public boolean registerPerson(String name, String surname, String email, String phone, LocalDate dateOfBirth) {
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

				// set AUTOINCREMENT start so next id will be 100 000, but don't decrease existing
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

					// set sqlite_sequence so next id >= 100 000 (don't lower existing seq)
					try {
						try (ResultSet rsSeq = stmt
								.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='person'")) {
							if (rsSeq.next()) {
								int seq = rsSeq.getInt("seq");
								if (seq < 99999) {
									stmt.execute("UPDATE sqlite_sequence SET seq = 99999 WHERE name='person'");
								}
							} else {
								stmt.execute("INSERT INTO sqlite_sequence(name, seq) VALUES('person', 99999);");
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
				// table exists and already has AUTOINCREMENT -> ensure sequence starts at 99999
				// (don't lower current value)
				try {
					try (ResultSet rsSeq = stmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='person'")) {
						if (rsSeq.next()) {
							int seq = rsSeq.getInt("seq");
							if (seq < 99999) {
								stmt.execute("UPDATE sqlite_sequence SET seq = 99999 WHERE name='person'");
							}
						} else {
							stmt.execute("INSERT INTO sqlite_sequence(name, seq) VALUES('person', 99999);");
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
				LocalDateTime now =LocalDateTime.now();
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

	public boolean recordDeparture(long personId) {
		// Find the latest open attendance record for today
		String updateSql = "UPDATE attendance SET attendanceEnd = ? WHERE idAtendance = (SELECT idAtendance FROM attendance WHERE idPerson = ? AND atendanceDay = ? AND attendanceEnd IS NULL ORDER BY idAtendance DESC LIMIT 1)";

		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

			LocalDateTime now =LocalDateTime.now();
			pstmt.setString(1, now.toLocalTime().toString());
			pstmt.setLong(2, personId);
			pstmt.setString(3, now.toLocalDate().toString());

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
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

	public boolean insertDocument(String name, String path, String language, String validFrom, String validTo) {
		String query = "INSERT INTO document (nameDoc, pathDoc, language, validityFrom, validityTo) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, name);
			pstmt.setString(2, path);
			pstmt.setString(3, language);
			pstmt.setString(4, validFrom);
			pstmt.setString(5, validTo); // Can be null
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<org.example.model.Document> getAllDocuments() {
		List<org.example.model.Document> documents = new ArrayList<>();
		String query = "SELECT * FROM document ORDER BY idDocument DESC";
		try (Connection conn = getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				documents.add(new org.example.model.Document(
						rs.getLong("idDocument"),
						rs.getString("nameDoc"),
						rs.getString("pathDoc"),
						rs.getString("language"),
						rs.getString("validityFrom"),
						rs.getString("validityTo")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return documents;
	}

	public boolean deleteDocument(long id) {
		String query = "DELETE FROM document WHERE idDocument = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setLong(1, id);
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

	public List<org.example.model.Person> searchPersons(String queryStr) {
		List<org.example.model.Person> persons = new ArrayList<>();
		String sql = "SELECT p.*, GROUP_CONCAT(pos.positionName, ', ') as positionNames " +
				"FROM person p " +
				"LEFT JOIN personHasPossition php ON p.idPerson = php.idPerson AND php.positionTo IS NULL " +
				"LEFT JOIN position pos ON php.idPosition = pos.idPosition " +
				"WHERE p.name LIKE ? OR p.surname LIKE ? OR p.email LIKE ? " +
				"GROUP BY p.idPerson";

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
							rs.getString("dateOfBirth"),
							rs.getString("positionNames"));
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

			int rows = pstmt.executeUpdate();
			// updatePersonPosition(person.getId(), person.getPosition()); // REMOVED:
			// Managed separately now
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<org.example.model.AttendanceRecord> getAttendance(int month, int year) {
		List<org.example.model.AttendanceRecord> records = new ArrayList<>();
		// SQLite strftime('%m', atendanceDay) returns e.g. '01', '12'.

		String sql = "SELECT DISTINCT a.idAtendance, p.name, p.surname, a.atendanceDay, a.attendanceStart, a.attendanceEnd "
				+
				"FROM attendance a " +
				"JOIN person p ON a.idPerson = p.idPerson " +
				"JOIN personHasPossition php ON p.idPerson = php.idPerson " +
				"WHERE strftime('%m', a.atendanceDay) = ? AND strftime('%Y', a.atendanceDay) = ? " +
				"AND php.positionTo IS NULL " +
				"ORDER BY a.atendanceDay DESC, a.attendanceStart DESC, p.surname ASC";

		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, String.format("%02d", month));
			pstmt.setString(2, String.valueOf(year));

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String start = rs.getString("attendanceStart");
					String end = rs.getString("attendanceEnd");

					if (start != null && start.length() > 5 && start.chars().filter(ch -> ch == ':').count() == 2) {
						start = start.substring(0, start.lastIndexOf(':'));
					}
					if (end != null && end.length() > 5 && end.chars().filter(ch -> ch == ':').count() == 2) {
						end = end.substring(0, end.lastIndexOf(':'));
					}

					records.add(new org.example.model.AttendanceRecord(
							rs.getLong("idAtendance"),
							rs.getString("name"),
							rs.getString("surname"),
							rs.getString("atendanceDay"),
							start,
							end));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}

	public List<org.example.model.AttendanceRecord> getVisitsByDate(String date) {
		List<org.example.model.AttendanceRecord> records = new ArrayList<>();
		String sql = "SELECT a.idAtendance, p.name, p.surname, a.atendanceDay, a.attendanceStart, a.attendanceEnd " +
				"FROM attendance a " +
				"JOIN person p ON a.idPerson = p.idPerson " +
				"WHERE a.atendanceDay = ? " +
				"AND NOT EXISTS (SELECT 1 FROM personHasPossition php WHERE php.idPerson = p.idPerson AND php.positionTo IS NULL) "
				+
				"ORDER BY a.attendanceStart DESC";

		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, date);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String start = rs.getString("attendanceStart");
					String end = rs.getString("attendanceEnd");

					if (start != null && start.length() > 5 && start.chars().filter(ch -> ch == ':').count() == 2) {
						start = start.substring(0, start.lastIndexOf(':'));
					}
					if (end != null && end.length() > 5 && end.chars().filter(ch -> ch == ':').count() == 2) {
						end = end.substring(0, end.lastIndexOf(':'));
					}

					records.add(new org.example.model.AttendanceRecord(
							rs.getLong("idAtendance"),
							rs.getString("name"),
							rs.getString("surname"),
							rs.getString("atendanceDay"),
							start,
							end));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}

	public boolean updateAttendance(long idAttendance, String start, String end) {
		String sql = "UPDATE attendance SET attendanceStart = ?, attendanceEnd = ? WHERE idAtendance = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, start);
			pstmt.setString(2, end);
			pstmt.setLong(3, idAttendance);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteAttendance(long idAttendance) {
		String sql = "DELETE FROM attendance WHERE idAtendance = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, idAttendance);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addAttendanceRecord(long personId, String date, String start, String end) {
		String sql = "INSERT INTO attendance (idPerson, atendanceDay, attendanceStart, attendanceEnd) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, personId);
			pstmt.setString(2, date);
			pstmt.setString(3, start);
			pstmt.setString(4, end);

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
