package com.ams.util;

import java.sql.*;

/**
 * Database Connection Utility for Oracle SQL*Plus
 * Advocate Management System
 */
public class DBConnection {
    // Oracle JDBC connection settings
    private static final String DRIVER   = "oracle.jdbc.OracleDriver";

    private static final String DEFAULT_DB_URL  = "jdbc:oracle:thin:@//localhost:1521/XE";
    private static final String DEFAULT_DB_USER = "System";
    private static final String DEFAULT_DB_PASS = "SYSTEM";

    private static final String DB_URL  = System.getProperty("oracle.db.url", System.getenv("ORACLE_DB_URL")  != null ? System.getenv("ORACLE_DB_URL")  : DEFAULT_DB_URL);
    private static final String DB_USER = System.getProperty("oracle.db.user", System.getenv("ORACLE_DB_USER") != null ? System.getenv("ORACLE_DB_USER") : DEFAULT_DB_USER);
    private static final String DB_PASS = System.getProperty("oracle.db.password", System.getenv("ORACLE_DB_PASSWORD") != null ? System.getenv("ORACLE_DB_PASSWORD") : DEFAULT_DB_PASS);

    private static String lastError = null;
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !isConnectionValid()) {
                Class.forName(DRIVER);
                try {
                    connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                } catch (SQLException primaryEx) {
                    lastError = "Primary connection failed: " + primaryEx.getMessage();
                    // Try SID-style fallback for compatibility
                    if (DB_URL.contains("/") && DB_URL.contains(":")) {
                        String fallback = DB_URL.replaceFirst("@//", "@").replaceFirst("/", ":");
                        try {
                            connection = DriverManager.getConnection(fallback, DB_USER, DB_PASS);
                            lastError = null;
                            System.out.println("DBConnection: connected to fallback URL " + fallback + " as " + DB_USER);
                        } catch (SQLException fallbackEx) {
                            lastError += "; fallback failed: " + fallbackEx.getMessage();
                        }
                    }
                }
                if (connection != null && !connection.isClosed()) {
                    connection.setAutoCommit(false);
                    lastError = null;
                }
            }
        } catch (ClassNotFoundException e) {
            lastError = "Oracle JDBC Driver not found: " + e.getMessage();
        } catch (SQLException e) {
            if (lastError == null) lastError = "Database connection error: " + e.getMessage();
        } catch (Exception e) {
            // Catch any other exceptions silently - application should work in demo mode
            lastError = "Database unavailable: " + e.getMessage();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private static boolean isConnectionValid() {
        try { return connection != null && connection.isValid(2); }
        catch (Exception e) { return false; }
    }

    public static boolean testConnection() {
        Connection con = getConnection();
        return con != null;
    }

    public static String getLastError() {
        if (lastError != null) return lastError;
        if (connection == null) return "Unknown DB error";
        return null;
    }

    /**
     * Get current DB connection status for UI display
     */
    public static String getStatus() {
        try {
            Connection con = getConnection();
            if (con != null && !con.isClosed() && con.isValid(2)) {
                return "Connected";
            }
        } catch (Exception e) {
            // ignore for status check
        }
        return "Disconnected" + (lastError != null ? ": " + lastError : "");
    }
}
