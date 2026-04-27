package com.ams.util;

import java.util.*;

/**
 * In-memory data store for demo mode - persists added items during session
 */
public class DemoDataStore {
    private static final Map<String, List<Map<String, Object>>> data = new HashMap<>();
    
    static {
        initializeDemoData();
    }
    
    private static void initializeDemoData() {
        // Initialize cases
        List<Map<String, Object>> cases = new ArrayList<>();
        addCase(cases, 1001, "State vs. Rajan", "Arun Patel", "CRIMINAL", "ACTIVE", 1, "2024-01-10", "High Court");
        addCase(cases, 1002, "Property Dispute", "Sunita Patel", "CIVIL", "PENDING", 2, "2024-02-15", "District Court");
        addCase(cases, 1003, "Kumar Divorce", "Raj Kumar", "FAMILY", "CLOSED", 3, "2023-11-20", "Family Court");
        data.put("CASES", cases);
        
        // Initialize hearings
        List<Map<String, Object>> hearings = new ArrayList<>();
        addHearing(hearings, 2001, "State vs Rajan", "28-DEC-2024", "10:00", "High Court", "Hall 3", "Arguments", "J. Sharma", "UPCOMING");
        addHearing(hearings, 2002, "Patel Property", "10-JAN-2025", "14:00", "District Court", "Rm 5", "Evidence", "J. Patel", "UPCOMING");
        addHearing(hearings, 2003, "Kumar Divorce", "05-NOV-2024", "11:00", "Family Court", "Rm 2", "Final", "J. Rajan", "COMPLETED");
        data.put("HEARINGS", hearings);
        
        // Initialize evidence
        List<Map<String, Object>> evidence = new ArrayList<>();
        addEvidence(evidence, 3001, 1001, "DOCUMENT", "Police Report", "10-JAN-2024", "ADMITTED", 1);
        addEvidence(evidence, 3002, 1001, "PHOTO", "Witness", "15-JAN-2024", "PENDING", 0);
        addEvidence(evidence, 3003, 1002, "WITNESS", "Neighbor", "20-FEB-2024", "ADMITTED", 1);
        data.put("EVIDENCE", evidence);
        
        // Initialize reminders
        List<Map<String, Object>> reminders = new ArrayList<>();
        addReminder(reminders, 4001, 1001, "2025-01-30", "File rejoinder", "HIGH", "PENDING");
        addReminder(reminders, 4002, 1002, "2025-02-10", "Prepare for hearing", "MEDIUM", "PENDING");
        data.put("REMINDERS", reminders);
        
        // Initialize communications
        List<Map<String, Object>> comms = new ArrayList<>();
        addComm(comms, 5001, "Arun Patel", 1001, "CALL", "20-DEC-2024", "Case discussion", "OUT");
        addComm(comms, 5002, "Sunita Patel", 1002, "EMAIL", "15-JAN-2025", "Status update", "OUT");
        data.put("COMMUNICATIONS", comms);
        
        // Initialize timelines
        List<Map<String, Object>> timelines = new ArrayList<>();
        addTimeline(timelines, 6001, 1001, "15-DEC-2024", "Investigation Started", "Documents collected from police", "ON_TRACK", "Await hearing");
        addTimeline(timelines, 6002, 1002, "20-NOV-2024", "Evidence Submitted", "All property documents filed", "ON_TRACK", "File counter-claim");
        addTimeline(timelines, 6003, 1003, "01-OCT-2024", "Settlement Proposed", "Settlement papers drafted", "DELAYED", "Await client approval");
        data.put("TIMELINES", timelines);
    }
    
    // Case helpers
    private static void addCase(List<Map<String, Object>> list, int id, String title, String client, 
                               String type, String status, int priority, String date, String court) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("title", title); m.put("client", client); m.put("type", type);
        m.put("status", status); m.put("priority", priority); m.put("date", date); m.put("court", court);
        list.add(m);
    }
    
    public static void addCaseToDemo(int id, String title, String client, String type, String status, int priority, String date, String court) {
        addCase(getCasesList(), id, title, client, type, status, priority, date, court);
    }
    
    // Hearing helpers
    private static void addHearing(List<Map<String, Object>> list, int id, String caseName, String date, String time,
                                   String court, String room, String purpose, String judge, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("case", caseName); m.put("date", date); m.put("time", time);
        m.put("court", court); m.put("room", room); m.put("purpose", purpose);
        m.put("judge", judge); m.put("status", status);
        list.add(m);
    }
    
    public static void addHearingToDemo(int id, String caseName, String date, String time, String court, 
                                       String room, String purpose, String judge, String status) {
        addHearing(getHearingsList(), id, caseName, date, time, court, room, purpose, judge, status);
    }
    
    // Evidence helpers
    private static void addEvidence(List<Map<String, Object>> list, int id, int caseId, String type, 
                                   String source, String date, String status, int verified) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("caseId", caseId); m.put("type", type); m.put("source", source);
        m.put("date", date); m.put("status", status); m.put("verified", verified);
        list.add(m);
    }
    
    public static void addEvidenceToDemo(int id, int caseId, String type, String source, String date, String status, int verified) {
        addEvidence(getEvidenceList(), id, caseId, type, source, date, status, verified);
    }
    
    // Reminder helpers
    private static void addReminder(List<Map<String, Object>> list, int id, int caseId, String dueDate, 
                                   String message, String priority, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("caseId", caseId); m.put("dueDate", dueDate);
        m.put("message", message); m.put("priority", priority); m.put("status", status);
        list.add(m);
    }
    
    public static void addReminderToDemo(int id, int caseId, String dueDate, String message, String priority, String status) {
        addReminder(getRemindersList(), id, caseId, dueDate, message, priority, status);
    }
    
    // Communication helpers
    private static void addComm(List<Map<String, Object>> list, int id, String clientName, int caseId, 
                               String mode, String date, String summary, String direction) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("client", clientName); m.put("caseId", caseId);
        m.put("mode", mode); m.put("date", date); m.put("summary", summary); m.put("direction", direction);
        list.add(m);
    }
    
    public static void addCommToDemo(int id, String clientName, int caseId, String mode, String date, String summary, String direction) {
        addComm(getCommsList(), id, clientName, caseId, mode, date, summary, direction);
    }
    
    // Timeline helpers
    private static void addTimeline(List<Map<String, Object>> list, int id, int caseId, String date, 
                                   String title, String description, String status, String nextStep) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("caseId", caseId); m.put("date", date);
        m.put("title", title); m.put("description", description);
        m.put("status", status); m.put("nextStep", nextStep);
        list.add(m);
    }
    
    public static void addTimelineToDemo(int id, int caseId, String date, String title, String description, String status, String nextStep) {
        addTimeline(getTimelinesList(), id, caseId, date, title, description, status, nextStep);
    }
    
    // Getters for data lists
    public static List<Map<String, Object>> getCasesList() {
        if (!data.containsKey("CASES")) {
            data.put("CASES", new ArrayList<>());
        }
        return data.get("CASES");
    }
    
    public static List<Map<String, Object>> getHearingsList() {
        if (!data.containsKey("HEARINGS")) {
            data.put("HEARINGS", new ArrayList<>());
        }
        return data.get("HEARINGS");
    }
    
    public static List<Map<String, Object>> getEvidenceList() {
        if (!data.containsKey("EVIDENCE")) {
            data.put("EVIDENCE", new ArrayList<>());
        }
        return data.get("EVIDENCE");
    }
    
    public static List<Map<String, Object>> getRemindersList() {
        if (!data.containsKey("REMINDERS")) {
            data.put("REMINDERS", new ArrayList<>());
        }
        return data.get("REMINDERS");
    }
    
    public static List<Map<String, Object>> getCommsList() {
        if (!data.containsKey("COMMUNICATIONS")) {
            data.put("COMMUNICATIONS", new ArrayList<>());
        }
        return data.get("COMMUNICATIONS");
    }
    
    public static List<Map<String, Object>> getTimelinesList() {
        if (!data.containsKey("TIMELINES")) {
            data.put("TIMELINES", new ArrayList<>());
        }
        return data.get("TIMELINES");
    }
    
    public static void clearAll() {
        data.clear();
        initializeDemoData();
    }
}
