package com.ams.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Communication Logging Utility
 * Stores all communication logs in LOG_COMMUNICATION table
 */
public class LogCommunicationUtil {
    
    public static final String LOG_TYPE_CALL = "CALL";
    public static final String LOG_TYPE_EMAIL = "EMAIL";
    public static final String LOG_TYPE_MESSAGE = "MESSAGE";
    public static final String LOG_TYPE_MEETING = "MEETING";
    
    public static final String FOLLOW_UP_PENDING = "PENDING";
    public static final String FOLLOW_UP_COMPLETED = "COMPLETED";
    public static final String FOLLOW_UP_SCHEDULED = "SCHEDULED";
    
    /**
     * Log a communication event
     */
    public static boolean logCommunication(int caseId, int clientId, int advocateId,
                                          String logType, String subject, String details,
                                          String notes, Date followUpDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            String sql = "INSERT INTO LOG_COMMUNICATION " +
                        "(log_comm_id, case_id, c_id, a_id, log_type, log_subject, " +
                        "log_details, log_notes, log_date, log_time, follow_up_date, follow_up_status) " +
                        "VALUES (log_comm_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSTIMESTAMP, ?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, caseId);
            pstmt.setInt(2, clientId);
            pstmt.setInt(3, advocateId);
            pstmt.setString(4, logType);
            pstmt.setString(5, subject);
            pstmt.setString(6, details);
            pstmt.setString(7, notes);
            
            if (followUpDate != null) {
                pstmt.setDate(8, new java.sql.Date(followUpDate.getTime()));
                pstmt.setString(9, FOLLOW_UP_PENDING);
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            }
            
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error logging communication: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * Get communication logs for a case
     */
    public static List<Map<String, Object>> getCommunicationLogs(int caseId) {
        List<Map<String, Object>> logs = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return logs;
            
            String sql = "SELECT log_comm_id, log_type, log_date, log_subject, " +
                        "log_details, follow_up_date, follow_up_status " +
                        "FROM LOG_COMMUNICATION " +
                        "WHERE case_id = " + caseId + " " +
                        "ORDER BY log_date DESC";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("log_id", rs.getInt("log_comm_id"));
                log.put("type", rs.getString("log_type"));
                log.put("date", rs.getDate("log_date"));
                log.put("subject", rs.getString("log_subject"));
                log.put("details", rs.getString("log_details"));
                log.put("followUpDate", rs.getDate("follow_up_date"));
                log.put("followUpStatus", rs.getString("follow_up_status"));
                logs.add(log);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving communication logs: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return logs;
    }
    
    /**
     * Get communication logs for an advocate
     */
    public static List<Map<String, Object>> getAdvocateLogs(int advocateId, int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return logs;
            
            String sql = "SELECT log_comm_id, case_id, log_type, log_date, log_subject, " +
                        "follow_up_status FROM LOG_COMMUNICATION " +
                        "WHERE a_id = " + advocateId + " " +
                        "ORDER BY log_date DESC " +
                        "FETCH FIRST " + limit + " ROWS ONLY";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("log_id", rs.getInt("log_comm_id"));
                log.put("case_id", rs.getInt("case_id"));
                log.put("type", rs.getString("log_type"));
                log.put("date", new SimpleDateFormat("dd-MMM-yyyy").format(rs.getDate("log_date")));
                log.put("subject", rs.getString("log_subject"));
                log.put("followUpStatus", rs.getString("follow_up_status"));
                logs.add(log);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving advocate logs: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return logs;
    }
    
    /**
     * Update follow-up status
     */
    public static boolean updateFollowUpStatus(int logId, String newStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            
            String sql = "UPDATE LOG_COMMUNICATION SET follow_up_status = ? WHERE log_comm_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, logId);
            
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating follow-up status: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * Get pending follow-ups for advocate
     */
    public static List<Map<String, Object>> getPendingFollowUps(int advocateId) {
        List<Map<String, Object>> followUps = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return followUps;
            
            String sql = "SELECT log_comm_id, case_id, log_subject, follow_up_date, log_notes " +
                        "FROM LOG_COMMUNICATION " +
                        "WHERE a_id = " + advocateId + " " +
                        "AND follow_up_status = '" + FOLLOW_UP_PENDING + "' " +
                        "AND follow_up_date IS NOT NULL " +
                        "AND follow_up_date <= TRUNC(SYSDATE) + 1 " +
                        "ORDER BY follow_up_date ASC";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> followUp = new HashMap<>();
                followUp.put("log_id", rs.getInt("log_comm_id"));
                followUp.put("case_id", rs.getInt("case_id"));
                followUp.put("subject", rs.getString("log_subject"));
                followUp.put("dueDate", new SimpleDateFormat("dd-MMM-yyyy").format(rs.getDate("follow_up_date")));
                followUp.put("notes", rs.getString("log_notes"));
                followUps.add(followUp);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving pending follow-ups: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return followUps;
    }
    
    /**
     * Get communication statistics
     */
    public static Map<String, Integer> getCommunicationStats(int caseId) {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return stats;
            
            String sql = "SELECT log_type, COUNT(*) as cnt FROM LOG_COMMUNICATION " +
                        "WHERE case_id = " + caseId + " " +
                        "GROUP BY log_type";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                stats.put(rs.getString("log_type"), rs.getInt("cnt"));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving communication stats: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return stats;
    }
}
