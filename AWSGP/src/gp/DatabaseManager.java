package gp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
	// This will connect to a file named 'applicant.db' in your project directory
	private static final String DB_PATH = "applicant_db.db";
	private static final String DB_URL = "jdbc:sqlite:A:/NC1605/UMS Data/applicant_db.db";

	private static Connection connection = null;

	public static Connection getConnection() throws SQLException {
		try {
			// Load the SQLite JDBC driver
			Class.forName("org.sqlite.JDBC");

			// Log database path for debugging
			System.out.println("Using database at: " + DB_URL);

			return DriverManager.getConnection(DB_URL);
		} catch (ClassNotFoundException e) {
			throw new SQLException("SQLite JDBC Driver not found", e);
		}
	}
	public static boolean updatePersonalDetailsByName(String name, String email, 
			String phone, String address, String dateOfBirth) {
		String sql = """
				UPDATE applicants 
				SET email = ?, phone = ?, address = ?, date_of_birth = ?
				WHERE name = ?
				""";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Log the update operation for debugging
			System.out.println("Updating applicant: " + name);
			System.out.println("Email: " + email);
			System.out.println("Phone: " + phone);
			System.out.println("Address: " + address);
			System.out.println("Date of Birth: " + dateOfBirth);

			// Set parameters in the prepared statement
			if (email != null && !email.trim().isEmpty()) {
				pstmt.setString(1, email);
			} else {
				pstmt.setNull(1, Types.VARCHAR);
			}

			if (phone != null && !phone.trim().isEmpty()) {
				pstmt.setString(2, phone);
			} else {
				pstmt.setNull(2, Types.VARCHAR);
			}

			if (address != null && !address.trim().isEmpty()) {
				pstmt.setString(3, address);
			} else {
				pstmt.setNull(3, Types.VARCHAR);
			}

			// Convert date format for SQLite (yyyy-MM-dd)
			if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
				try {
					java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
					java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
					java.util.Date date = inputFormat.parse(dateOfBirth);
					String sqlDateStr = outputFormat.format(date);
					pstmt.setString(4, sqlDateStr);
					System.out.println("Converted date for SQL: " + sqlDateStr);
				} catch (Exception e) {
					pstmt.setNull(4, Types.DATE);
					System.err.println("Error parsing date: " + e.getMessage());
				}
			} else {
				pstmt.setNull(4, Types.DATE);
			}

			pstmt.setString(5, name);

			// Execute the update
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("Rows updated: " + rowsAffected);

			// Return true if at least one row was updated
			return rowsAffected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error updating applicant details: " + e.getMessage());
			return false;
		}
	}

	// Method to verify if applicant exists in database by name
	public static boolean verifyApplicantExistsByName(String name) {
		String sql = "SELECT COUNT(*) FROM applicants WHERE name = ?";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, name);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.println("Found " + count + " records for applicant name: " + name);
					return count > 0;
				}
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Direct update test by name
	public static boolean directUpdateTestByName(String name) {
		String sql = "UPDATE applicants SET email = 'test@example.com' WHERE name = ?";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, name);

			System.out.println("Test update for applicant name: " + name);
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("Test rows updated: " + rowsAffected);

			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	public static void initializeDatabase() throws SQLException {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement()) {

			// Create applicants table
			stmt.execute("""
					    CREATE TABLE IF NOT EXISTS applicants (
					        id VARCHAR(10) PRIMARY KEY,
					        name VARCHAR(100) NOT NULL,
					        date_of_application DATE NOT NULL,
					        certificate VARCHAR(100) NOT NULL,
					        grade VARCHAR(50) NOT NULL,
					        email VARCHAR(100),
					        phone VARCHAR(20),
					        address TEXT,
					        date_of_birth DATE
					    )
					""");

			// Create documents table
			stmt.execute("""
					    CREATE TABLE IF NOT EXISTS documents (
					        id INTEGER PRIMARY KEY AUTOINCREMENT,
					        applicant_id VARCHAR(10) NOT NULL,
					        document_type VARCHAR(50) NOT NULL,
					        file_path TEXT NOT NULL,
					        upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					        FOREIGN KEY (applicant_id) REFERENCES applicants(id)
					    )
					""");

			// Create comments table
			stmt.execute("""
					    CREATE TABLE IF NOT EXISTS comments (
					        id INTEGER PRIMARY KEY AUTOINCREMENT,
					        applicant_id VARCHAR(10) NOT NULL,
					        comment_text TEXT NOT NULL,
					        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					        FOREIGN KEY (applicant_id) REFERENCES applicants(id)
					    )
					""");

			System.out.println("Database tables created successfully!");
		}
	}

	// Update personal details in the database
	public static boolean updatePersonalDetails(String applicantId, String email, 
			String phone, String address, String dateOfBirth) {
		String sql = """
				    UPDATE applicants 
				    SET email = ?, phone = ?, address = ?, date_of_birth = ?
				    WHERE id = ?
				""";

		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Log the update operation for debugging
			System.out.println("Updating applicant: " + applicantId);
			System.out.println("Email: " + email);
			System.out.println("Phone: " + phone);
			System.out.println("Address: " + address);
			System.out.println("Date of Birth: " + dateOfBirth);

			// Set parameters in the prepared statement
			if (email != null && !email.trim().isEmpty()) {
				pstmt.setString(1, email);
			} else {
				pstmt.setNull(1, Types.VARCHAR);
			}

			if (phone != null && !phone.trim().isEmpty()) {
				pstmt.setString(2, phone);
			} else {
				pstmt.setNull(2, Types.VARCHAR);
			}

			if (address != null && !address.trim().isEmpty()) {
				pstmt.setString(3, address);
			} else {
				pstmt.setNull(3, Types.VARCHAR);
			}

			// Convert date format for SQLite (yyyy-MM-dd)
			if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
				try {
					java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
					java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
					java.util.Date date = inputFormat.parse(dateOfBirth);
					String sqlDateStr = outputFormat.format(date);
					pstmt.setString(4, sqlDateStr);
					System.out.println("Converted date for SQL: " + sqlDateStr);
				} catch (Exception e) {
					pstmt.setNull(4, Types.DATE);
					System.err.println("Error parsing date: " + e.getMessage());
				}
			} else {
				pstmt.setNull(4, Types.DATE);
			}

			pstmt.setString(5, applicantId);

			// Execute the update
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("Rows updated: " + rowsAffected);

			// Return true if at least one row was updated
			return rowsAffected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error updating applicant details: " + e.getMessage());
			return false;
		}
	}
	// Add these methods to your DatabaseManager class

	/**
	 * Saves a document reference to the database
	 * 
	 * @param applicantId The ID of the applicant
	 * @param documentType The type of document
	 * @param filePath The path to the stored file
	 * @return true if save was successful
	 */
	public static boolean saveDocument(String applicantId, String documentType, String filePath) {
	    String sql = "INSERT INTO documents (applicant_id, document_type, file_path) VALUES (?, ?, ?)";
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, applicantId);
	        pstmt.setString(2, documentType);
	        pstmt.setString(3, filePath);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error saving document: " + e.getMessage());
	        return false;
	    }
	}

	/**
	 * Retrieves all documents for an applicant
	 * 
	 * @param applicantId The ID of the applicant
	 * @return A map of document types to file paths
	 */
	public static Map<String, String> getDocuments(String applicantId) {
	    Map<String, String> documents = new HashMap<>();
	    String sql = "SELECT document_type, file_path FROM documents WHERE applicant_id = ?";
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, applicantId);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                String docType = rs.getString("document_type");
	                String filePath = rs.getString("file_path");
	                documents.put(docType, filePath);
	            }
	        }
	        
	        return documents;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error retrieving documents: " + e.getMessage());
	        return documents;
	    }
	}

	/**
	 * Updates an existing document in the database
	 * 
	 * @param applicantId The ID of the applicant
	 * @param documentType The type of document
	 * @param filePath The new file path
	 * @return true if update was successful
	 */
	public static boolean updateDocument(String applicantId, String documentType, String filePath) {
	    // First check if document exists
	    if (documentExists(applicantId, documentType)) {
	        String sql = "UPDATE documents SET file_path = ? WHERE applicant_id = ? AND document_type = ?";
	        
	        try (Connection conn = getConnection();
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            
	            pstmt.setString(1, filePath);
	            pstmt.setString(2, applicantId);
	            pstmt.setString(3, documentType);
	            
	            int rowsAffected = pstmt.executeUpdate();
	            return rowsAffected > 0;
	            
	        } catch (SQLException e) {
	            e.printStackTrace();
	            System.err.println("Error updating document: " + e.getMessage());
	            return false;
	        }
	    } else {
	        // If document doesn't exist, save it as new
	        return saveDocument(applicantId, documentType, filePath);
	    }
	}
	//I generated random id without dob as the user has yet to input their dob even though I used the name of the applicant
	public static String generateApplicantId(String name) {
	    if (name == null || name.trim().isEmpty()) {
	        return "UNK_0001"; // Default for unknown names
	    }
	    
	    // Extract first three letters from name (convert to uppercase)
	    String namePrefix = "";
	    
	    // Remove spaces and special characters
	    String cleanName = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
	    
	    if (cleanName.length() >= 3) {
	        namePrefix = cleanName.substring(0, 3);
	    } else if (cleanName.length() > 0) {
	        // If name is less than 3 characters, pad with 'X'
	        namePrefix = cleanName + "X".repeat(3 - cleanName.length());
	    } else {
	        // If no valid characters at all, use UNK
	        namePrefix = "UNK";
	    }
	    
	    // Find the highest existing number for this prefix
	    int highestNumber = 0;
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(
	             "SELECT id FROM applicants WHERE id LIKE ?")) {
	        
	        pstmt.setString(1, namePrefix + "_%");
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                String existingId = rs.getString("id");
	                // Extract the number part
	                if (existingId != null && existingId.length() > 4 && existingId.contains("_")) {
	                    try {
	                        String numPart = existingId.substring(existingId.indexOf("_") + 1);
	                        if (numPart.matches("\\d+")) {
	                            int existingNumber = Integer.parseInt(numPart);
	                            if (existingNumber > highestNumber) {
	                                highestNumber = existingNumber;
	                            }
	                        }
	                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
	                        // Ignore malformed IDs
	                        System.err.println("Malformed ID found: " + existingId);
	                    }
	                }
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error getting highest ID number: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    // Increment the highest number
	    int newNumber = highestNumber + 1;
	    
	    // Format the ID
	    return namePrefix + "_" + String.format("%04d", newNumber);
	}


	public static boolean documentExists(String applicantId, String documentType) {
	    String sql = "SELECT COUNT(*) FROM documents WHERE applicant_id = ? AND document_type = ?";
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, applicantId);
	        pstmt.setString(2, documentType);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	        
	        return false;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error checking document existence: " + e.getMessage());
	        return false;
	    }
	}

	/**
	 * Deletes a document from the database
	 * 
	 * @param applicantId The ID of the applicant
	 * @param documentType The type of document
	 * @return true if delete was successful
	 */
	public static boolean deleteDocument(String applicantId, String documentType) {
	    String sql = "DELETE FROM documents WHERE applicant_id = ? AND document_type = ?";
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, applicantId);
	        pstmt.setString(2, documentType);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error deleting document: " + e.getMessage());
	        return false;
	    }
	}
	
	/**
	 * Add this method to test that the ID generation is working
	 */
	public static void testIdGeneration() {
	    System.out.println("Test ID for 'John Smith': " + generateApplicantId("John Smith"));
	    System.out.println("Test ID for 'A': " + generateApplicantId("A"));
	    System.out.println("Test ID for '$p3c!al Ch@rs': " + generateApplicantId("$p3c!al Ch@rs"));
	}
	/**
	 * Deletes all documents for an applicant from the database
	 * 
	 * @param applicantId The ID of the applicant
	 * @return true if delete was successful
	 */
	public static boolean deleteAllDocuments(String applicantId) {
	    String sql = "DELETE FROM documents WHERE applicant_id = ?";
	    
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, applicantId);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error deleting all documents: " + e.getMessage());
	        return false;
	    }
	}
	
	// Method to check if the database exists and has required tables
	public static boolean checkDatabaseStructure() {
		try (Connection conn = getConnection()) {
			DatabaseMetaData metaData = conn.getMetaData();

			// Check if applicants table exists
			ResultSet tables = metaData.getTables(null, null, "applicants", null);
			boolean applicantsTableExists = tables.next();
			tables.close();

			if (!applicantsTableExists) {
				System.out.println("Applicants table doesn't exist - creating database structure");
				initializeDatabase();
				return true;
			}

			// Check if applicants table has the required columns
			ResultSet columns = metaData.getColumns(null, null, "applicants", "email");
			boolean emailColumnExists = columns.next();
			columns.close();

			columns = metaData.getColumns(null, null, "applicants", "phone");
			boolean phoneColumnExists = columns.next();
			columns.close();

			columns = metaData.getColumns(null, null, "applicants", "address");
			boolean addressColumnExists = columns.next();
			columns.close();

			columns = metaData.getColumns(null, null, "applicants", "date_of_birth");
			boolean dobColumnExists = columns.next();
			columns.close();

			// If any required column is missing, alter the table
			if (!emailColumnExists || !phoneColumnExists || !addressColumnExists || !dobColumnExists) {
				System.out.println("Required columns missing - updating database structure");

				Statement stmt = conn.createStatement();

				if (!emailColumnExists) {
					stmt.execute("ALTER TABLE applicants ADD COLUMN email VARCHAR(100)");
				}

				if (!phoneColumnExists) {
					stmt.execute("ALTER TABLE applicants ADD COLUMN phone VARCHAR(20)");
				}

				if (!addressColumnExists) {
					stmt.execute("ALTER TABLE applicants ADD COLUMN address TEXT");
				}

				if (!dobColumnExists) {
					stmt.execute("ALTER TABLE applicants ADD COLUMN date_of_birth DATE");
				}

				stmt.close();
			}

			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}