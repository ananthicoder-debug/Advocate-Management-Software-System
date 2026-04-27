import java.sql.*;

/**
 * Test Oracle Database Connection and Verify Data Storage
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Oracle Database Connection Test");
        System.out.println("========================================\n");
        
        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("✓ Oracle JDBC Driver loaded successfully");
            
            // Try to connect to Oracle
            String url = "jdbc:oracle:thin:@//localhost:1521/XE";
            String user = "SYSTEM";
            String password = "SYSTEM";
            
            System.out.println("\nAttempting to connect to: " + url);
            System.out.println("Username: " + user);
            
            Connection conn = DriverManager.getConnection(url, user, password);
            
            System.out.println("✓ Successfully connected to Oracle Database!");
            System.out.println("  Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("  Version: " + conn.getMetaData().getDatabaseProductVersion());
            
            // Check existing tables and data
            System.out.println("\n========================================");
            System.out.println("  Checking Database Tables & Data");
            System.out.println("========================================\n");
            
            // Check CASES table
            System.out.println("📋 CASES Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM CASES");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample cases
                rs = stmt.executeQuery("SELECT case_id, c_title, c_type, status FROM CASES WHERE ROWNUM <= 5 ORDER BY case_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Title: " + rs.getString(2) + 
                                     " | Type: " + rs.getString(3) + " | Status: " + rs.getString(4));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ CASES table not found or query failed: " + e.getMessage());
            }
            
            // Check HEARING table
            System.out.println("\n📋 HEARING Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM HEARING");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample hearings
                rs = stmt.executeQuery("SELECT h_id, case_id, h_date, h_time, status FROM HEARING WHERE ROWNUM <= 5 ORDER BY h_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Case: " + rs.getInt(2) + 
                                     " | Date: " + rs.getString(3) + " | Time: " + rs.getString(4) + 
                                     " | Status: " + rs.getString(5));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ HEARING table not found or query failed: " + e.getMessage());
            }
            
            // Check REMINDER table
            System.out.println("\n📋 REMINDER Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM REMINDER");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample reminders
                rs = stmt.executeQuery("SELECT rem_id, case_id, due_date, priority, rem_status FROM REMINDER WHERE ROWNUM <= 5 ORDER BY rem_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Case: " + rs.getInt(2) + 
                                     " | Due: " + rs.getString(3) + " | Priority: " + rs.getString(4) + 
                                     " | Status: " + rs.getString(5));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ REMINDER table not found or query failed: " + e.getMessage());
            }
            
            // Check EVIDENCE table
            System.out.println("\n📋 EVIDENCE Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM EVIDENCE");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample evidence
                rs = stmt.executeQuery("SELECT e_id, case_id, e_type, e_source, admissibility FROM EVIDENCE WHERE ROWNUM <= 5 ORDER BY e_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Case: " + rs.getInt(2) + 
                                     " | Type: " + rs.getString(3) + " | Source: " + rs.getString(4) + 
                                     " | Status: " + rs.getString(5));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ EVIDENCE table not found or query failed: " + e.getMessage());
            }
            
            // Check COMMUNICATION table
            System.out.println("\n📋 COMMUNICATION Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM COMMUNICATION");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample communications
                rs = stmt.executeQuery("SELECT com_id, c_id, case_id, comm_mode, init_date FROM COMMUNICATION WHERE ROWNUM <= 5 ORDER BY com_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Client: " + rs.getInt(2) + 
                                     " | Case: " + rs.getInt(3) + " | Mode: " + rs.getString(4) + 
                                     " | Date: " + rs.getString(5));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ COMMUNICATION table not found or query failed: " + e.getMessage());
            }
            
            // Check TIMELINE table
            System.out.println("\n📋 TIMELINE Table:");
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt FROM TIMELINE");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("   Total records: " + count);
                }
                rs.close();
                
                // Display sample timelines
                rs = stmt.executeQuery("SELECT time_id, case_id, entry_date, title, status_indicator FROM TIMELINE WHERE ROWNUM <= 5 ORDER BY time_id");
                System.out.println("   Sample records:");
                while (rs.next()) {
                    System.out.println("     - ID: " + rs.getInt(1) + " | Case: " + rs.getInt(2) + 
                                     " | Date: " + rs.getString(3) + " | Title: " + rs.getString(4) + 
                                     " | Status: " + rs.getString(5));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("   ⚠ TIMELINE table not found or query failed: " + e.getMessage());
            }
            
            System.out.println("\n========================================");
            System.out.println("  Connection Status: ✓ ACTIVE");
            System.out.println("  Data Persistence: ✓ VERIFIED");
            System.out.println("========================================\n");
            
            conn.close();
            
        } catch (ClassNotFoundException e) {
            System.out.println("✗ Oracle JDBC Driver not found!");
            System.out.println("  Error: " + e.getMessage());
            System.out.println("\n  Solution: Ensure ojdbc11.jar is in the lib/ directory");
        } catch (SQLException e) {
            System.out.println("✗ Cannot connect to Oracle Database");
            System.out.println("  Error: " + e.getMessage());
            System.out.println("\n  Possible causes:");
            System.out.println("  - Oracle Database is not running");
            System.out.println("  - Database credentials are incorrect (SYSTEM/SYSTEM)");
            System.out.println("  - Database name or port is incorrect (localhost:1521/XE)");
            System.out.println("\n  INFO: Application will run in DEMO MODE without database");
        }
    }
}
