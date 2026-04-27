package com.ams.util;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Client Communication Management Utility
 * Stores and retrieves client-advocate communications with status tracking
 */
public class ClientCommunicationUtil {
    
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_READ = "READ";
    public static final String STATUS_PENDING = "PENDING";
    
    public static final String MODE_CALL = "CALL";
    public static final String MODE_EMAIL = "EMAIL";
    public static final String MODE_MESSAGE = "MESSAGE";
    public static final String MODE_MEETING = "MEETING";
    public static final String MODE_CHAT = "CHAT";
    
    /**
     * Store a new communication
     */
    public static boolean storeCommunication(int caseId, int clientId, int advocateId,
                                            String messageText, String commMode,
                                            String subject, String filePath) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            
            String sql = "INSERT INTO CLIENT_COMMUNICATION " +
                        "(comm_id, case_id, c_id, a_id, message_text, sent_date, " +
                        "comm_status, comm_mode, direction, subject, file_attached, file_path) " +
                        "VALUES (client_comm_seq.NEXTVAL, ?, ?, ?, ?, SYSDATE, ?, ?, 'OUT', ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, caseId);
            pstmt.setInt(2, clientId);
            pstmt.setInt(3, advocateId);
            pstmt.setString(4, messageText);
            pstmt.setString(5, STATUS_SENT);
            pstmt.setString(6, commMode);
            pstmt.setString(7, subject);
            pstmt.setString(8, (filePath != null && !filePath.isEmpty()) ? "Y" : "N");
            pstmt.setString(9, filePath);
            
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error storing communication: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * Get communications for a case with optional status filter
     */
    public static List<Map<String, Object>> getCommunications(int caseId, String statusFilter) {
        List<Map<String, Object>> communications = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return communications;
            
            String sql = "SELECT c.comm_id, cl.c_name, c.message_text, c.sent_date, " +
                        "c.comm_status, c.comm_mode, c.subject, c.file_attached " +
                        "FROM CLIENT_COMMUNICATION c " +
                        "LEFT JOIN CLIENT1 cl ON c.c_id = cl.c_id " +
                        "WHERE c.case_id = ? ";
            
            if (statusFilter != null && !statusFilter.isEmpty() && !"All".equalsIgnoreCase(statusFilter)) {
                sql += "AND c.comm_status = ? ";
            }
            
            sql += "ORDER BY c.sent_date DESC";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, caseId);
            
            if (statusFilter != null && !statusFilter.isEmpty() && !"All".equalsIgnoreCase(statusFilter)) {
                pstmt.setString(2, statusFilter);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> comm = new HashMap<>();
                comm.put("comm_id", rs.getInt("comm_id"));
                comm.put("client_name", rs.getString("c_name"));
                comm.put("message", rs.getString("message_text"));
                comm.put("sent_date", new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(rs.getTimestamp("sent_date")));
                comm.put("status", rs.getString("comm_status"));
                comm.put("mode", rs.getString("comm_mode"));
                comm.put("subject", rs.getString("subject"));
                comm.put("hasFile", "Y".equals(rs.getString("file_attached")));
                communications.add(comm);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving communications: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException ignored) {}
        }
        return communications;
    }
    
    /**
     * Update communication status
     */
    public static boolean updateCommunicationStatus(int commId, String newStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            
            String sql = "UPDATE CLIENT_COMMUNICATION SET comm_status = ? WHERE comm_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, commId);
            
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating communication status: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * Get unread communications count for advocate
     */
    public static int getUnreadCount(int advocateId) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return 0;
            
            String sql = "SELECT COUNT(*) as cnt FROM CLIENT_COMMUNICATION " +
                        "WHERE a_id = " + advocateId + " " +
                        "AND comm_status IN ('" + STATUS_RECEIVED + "', '" + STATUS_PENDING + "')";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return 0;
    }
    
    /**
     * Get communication summary by mode
     */
    public static Map<String, Integer> getCommunicationSummary(int caseId) {
        Map<String, Integer> summary = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return summary;
            
            String sql = "SELECT comm_mode, COUNT(*) as cnt FROM CLIENT_COMMUNICATION " +
                        "WHERE case_id = " + caseId + " " +
                        "GROUP BY comm_mode";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                summary.put(rs.getString("comm_mode"), rs.getInt("cnt"));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting communication summary: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return summary;
    }
    
    /**
     * Get recent communications for dashboard
     */
    public static List<Map<String, Object>> getRecentCommunications(int advocateId, int limit) {
        List<Map<String, Object>> comms = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return comms;
            
            String sql = "SELECT c.comm_id, cl.c_name, cs.c_title, c.message_text, " +
                        "c.sent_date, c.comm_status " +
                        "FROM CLIENT_COMMUNICATION c " +
                        "LEFT JOIN CLIENT1 cl ON c.c_id = cl.c_id " +
                        "LEFT JOIN CASES1 cs ON c.case_id = cs.case_id " +
                        "WHERE c.a_id = " + advocateId + " " +
                        "ORDER BY c.sent_date DESC " +
                        "FETCH FIRST " + limit + " ROWS ONLY";
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> comm = new HashMap<>();
                comm.put("comm_id", rs.getInt("comm_id"));
                comm.put("client", rs.getString("c_name"));
                comm.put("case", rs.getString("c_title"));
                String msg = rs.getString("message_text");
                if (msg != null && msg.length() > 60) {
                    msg = msg.substring(0, 60) + "...";
                }
                comm.put("message", msg);
                comm.put("time", new SimpleDateFormat("HH:mm").format(rs.getTimestamp("sent_date")));
                comm.put("status", rs.getString("comm_status"));
                comms.add(comm);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting recent communications: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return comms;
    }
}
