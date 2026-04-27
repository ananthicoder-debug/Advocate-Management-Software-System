# AMS 18-Requirement Implementation Guide

## Database Changes Already Added
All database schema updates have been created in: `sql/schema_updates.sql`

Execute this file to add all new tables:
- CLIENT_COMMUNICATION
- MESSAGE_CHAT  
- EVIDENCE_FILE
- CASE_TIMELINE_EVENT
- TASK_DOCUMENT
- TASK_SUBMISSION
- LOG_COMMUNICATION
- NOTIFICATION
- ADVOCATE_PHOTO, JUNIOR_PHOTO, STAFF_PHOTO

## Code Files Created

### 1. com.ams.util.FileUploadDownloadUtil
- Centralized file upload/download management
- Supports evidence, documents, and photos
- Methods: chooseEvidenceFile(), chooseDocumentFile(), choosePhotoFile(), downloadFile(), openFile()

### 2. com.ams.components.PhotoUploadDialog  
- Reusable photo upload dialog
- With preview functionality
- Used for Advocate, Junior, and Staff profiles

### 3. com.ams.advocate.AddTimelineEventDialog
- Add new timeline events for cases
- Includes date, time, type, description
- Stores in CASE_TIMELINE_EVENT table

### 4. com.ams.login.AddClientDialog
- Add/Edit client functionality
- Client information form
- Integrated with CLIENT1 table

### 5. com.ams.dashboard.AddSupportStaffDialog
- Add/Edit support staff functionality
- Department and role assignment
- Integrated with STAFF1 table

## Implementation Steps for Each Requirement

### 1. LEGAL NOTES - Oracle Connection
**Status**: Already exists in schema (NOTE1 table)
**Fix**: Ensure legal notes form properly connects to NOTE1 table
```java
// In LegalNotesPage or CaseJourneyDialog:
String sql = "INSERT INTO NOTE1 (n_id, a_id, case_id, analysis, en_data, followup_q, imp_judgement, con_status) " +
             "VALUES (note_seq.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?, ?)";
```

### 2. CLIENT COMMUNICATION - Database & Workflow
**Files to modify**:
- `advocate/CommunicationsPage.java` - Add status filter dropdown
- Query needs to add status filter:
```java
ResultSet rs = c.prepareStatement(
    "SELECT * FROM CLIENT_COMMUNICATION WHERE case_id=? AND a_id=? " +
    "AND LOWER(comm_status) LIKE LOWER('%' + ? + '%')" +
    " ORDER BY sent_date DESC"
).executeQuery();
```

### 3. CLIENT COMMUNICATION STATUS FILTER
**Add to CommunicationsPage**:
```java
JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "SENT", "RECEIVED", "READ", "PENDING"});
statusFilter.addActionListener(e -> loadCommsWithFilter((String)statusFilter.getSelectedItem()));
```

### 4. EVIDENCE FILE UPLOAD & VIEWING
**Files to modify**: `advocate/EvidencePage.java`
**Add buttons**:
- "Upload File" button - calls FileUploadDownloadUtil.chooseEvidenceFile()
- "View File" button - calls FileUploadDownloadUtil.openFile()
- "Download" button - calls FileUploadDownloadUtil.downloadFile()
```java
String filePath = FileUploadDownloadUtil.chooseEvidenceFile(this);
if (filePath != null) {
    // Save to EVIDENCE_FILE table
    storeEvidenceFile(caseId, filePath);
}
```

### 5. CASE TIMELINE - ADD EVENT  
**Files to modify**: `advocate/TimelinePage.java`
**Add button**: "+ Add Timeline Event"
```java
JButton addEventBtn = new RoundedButton("+ Add Event");
addEventBtn.addActionListener(e -> {
    AddTimelineEventDialog dialog = new AddTimelineEventDialog(frame, caseId);
    dialog.setVisible(true);
    if (dialog.isSaved()) {
        refreshTimeline();
    }
});
```

### 6. LOG COMMUNICATION - Database Storage
**Create LogCommunicationUtil.java**:
```java
public static void logCommunication(int caseId, int clientId, int advocateId, 
    String logType, String subject, String details, String notes) {
    String sql = "INSERT INTO LOG_COMMUNICATION (log_comm_id, case_id, c_id, a_id, " +
                "log_type, log_subject, log_details, log_notes, log_date, log_time) " +
                "VALUES (log_comm_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSTIMESTAMP)";
    // Execute insert
}
```

### 7. NOTIFICATION BELL - Reminders
**Create NotificationBellComponent.java**:
```java
public class NotificationBellComponent extends JPanel {
    private JLabel bellLabel;
    private JPopupMenu notificationMenu;
    private List<Notification> notifications;
    
    // Show notifications for today and tomorrow
    // Mark as done removes from list
    // Priority ordering
}
```
**Add to dashboard header - next to user profile**

### 8. PHOTO UPLOAD - Junior Profile
**Files to modify**: `dashboard/JuniorDashboard.java`  
**Add button**: "Upload Photo" in profile section
```java
PhotoUploadDialog photoDialog = new PhotoUploadDialog(frame, "Upload Profile Photo");
photoDialog.setVisible(true);
if (photoDialog.isConfirmed()) {
    String photoPath = photoDialog.getUploadedPhotoPath();
    // Update JUNIOR_PHOTO table
}
```

### 9. TASK SUBMISSION - Junior Task List
**Modify**: `advocate/TaskAssignmentPage.java`
**Add UI for**:
- View assigned tasks
- Submit task with file upload
- Track submission status
- Senior verification workflow

### 10. MESSAGE CHAT - WhatsApp Style
**Create**: `util/ChatMessageUtil.java`
**Create MessageChatDialog.java**:
- Display messages in chat bubbles
- Different colors for sent/received
- Timestamp display
- File attachment support

### 11. STAFF PROFILE PHOTO & UPDATE
**Files to modify**: `dashboard/StaffDashboard.java`
**Add**:
- Photo upload button
- Profile update with larger font
- Bigger text input fields (40-50px height)
- Better visible form layout

### 12. ADD JUNIOR SCREEN - Full Screen
**Files to modify**: `dashboard/JuniorManagePage.java`
**Show as full-screen dialog or tab**:
- All information visible at once
- No scrolling needed if possible
- Larger form fields
- Big buttons

### 13. FIX DUPLICATE ADVOCATE SELECTION
**Issue**: When managing credentials, asks "Select an advocate" twice
**Location**: Likely in `dashboard/StaffDashboard.java` in Junior Advocates section
**Fix**: Remove duplicate prompt in credentials dialog

### 14. ADD CLIENT - Management System
**Files to modify**: `dashboard/ClientsPage.java`
**Add button**: "+ Add Client"
```java
JButton addClientBtn = new RoundedButton("+ Add Client");
addClientBtn.addActionListener(e -> {
    AddClientDialog dialog = new AddClientDialog(frame, null);
    dialog.setVisible(true);
    if (dialog.isSaved()) {
        refreshClientList();
    }
});
```

### 15. ADD SUPPORT STAFF - Management
**Files to modify**: `dashboard/StaffDashboard.java` or create SupportStaffPage.java
**Add button**: "+ Add Support Staff"
```java
JButton addStaffBtn = new RoundedButton("+ Add Support Staff");
addStaffBtn.addActionListener(e -> {
    AddSupportStaffDialog dialog = new AddSupportStaffDialog(frame, null);
    dialog.setVisible(true);
    if (dialog.isSaved()) {
        refreshStaffList();
    }
});
```

### 16. CASE DETAILS - From Case Management
**Files to modify**: Case management table/list view
**Add**: Double-click or "View" button to show full case journey
```java
// When user clicks on a case:
new CaseJourneyDialog(frame, caseId).setVisible(true);
```

### 17. PAYMENT RECORDS - Clean Up Data
**Remove irrelevant data**:
```sql
-- Clean up duplicate payment records
DELETE FROM PAYMENT1 WHERE pay_id IN (
    SELECT pay_id FROM PAYMENT1 p
    WHERE EXISTS (
        SELECT 1 FROM PAYMENT1 p2 
        WHERE p2.case_id = p.case_id 
        AND p2.pay_date = p.pay_date
        AND p2.pay_id < p.pay_id
    )
);

-- Verify and update payment display to show only relevant columns
-- Focus on: case_id, amount, pay_date, pay_type, bill_no
```

### 18. REMOVE DUPLICATE ADVOCATES
**Run data cleanup**:
```sql
-- Find duplicates
SELECT a_name, COUNT(*) as cnt FROM ADVOCATE1 GROUP BY a_name HAVING COUNT(*) > 1;

-- Remove duplicates, keep the one with highest rating
DELETE FROM ADVOCATE1 WHERE a_id IN (
    SELECT a_id FROM (
        SELECT a_id, ROW_NUMBER() OVER (PARTITION BY a_name ORDER BY rating DESC) as rn
        FROM ADVOCATE1
    ) WHERE rn > 1
);

-- Verify no orphaned relationships
DELETE FROM ADVOCATE_CASE1 WHERE a_id NOT IN (SELECT a_id FROM ADVOCATE1);
DELETE FROM JUNIOR_CASE1 WHERE granted_by NOT IN (SELECT a_id FROM ADVOCATE1);
```

## Integration Steps

1. **First**: Run `sql/schema_updates.sql` to create new tables
2. **Second**: Add all new Java classes to project
3. **Third**: Run data cleanup scripts from section 17-18
4. **Fourth**: Modify existing pages (CommunicationsPage, EvidencePage, etc.)
5. **Fifth**: Add NotificationBell component to dashboard headers
6. **Sixth**: Test all functionality

## Important Configuration

### File Upload Paths
```
/uploads/
  /evidence/       - Evidence files (images, videos, PDFs)
  /documents/      - Task submission documents
  /photos/         - Profile photos
```

### File Size Limits
- Photos: 5MB max
- Evidence: 50MB max  
- Documents: 50MB max

### Supported File Types
- Photos: JPG, PNG only
- Evidence: JPG, PNG, GIF, BMP, MP4, AVI, MOV, MKV, PDF
- Documents: DOC, DOCX, XLSX, XLS, PDF, PPT, TXT

## Testing Checklist

- [ ] Database connection test with schema_updates.sql
- [ ] File upload/download functionality
- [ ] Photo upload for profiles
- [ ] Timeline event creation
- [ ] Client add/edit workflow
- [ ] Support staff add/edit workflow
- [ ] Notification bell functionality
- [ ] Task submission workflow
- [ ] Message chat display
- [ ] Communication status filtering
- [ ] Evidence viewing
- [ ] Data cleanup (no duplicates)
- [ ] All screens render without errors
- [ ] Database constraints properly enforced

## Troubleshooting

If getting Oracle errors:
- Verify DBConnection.java can connect
- Check table names match exactly (case-sensitive)
- Run schema_updates.sql successfully first
- Verify sequence names match (e.g., client_comm_seq)

If file upload not working:
- Check `/uploads/` directory exists
- Verify file permissions
- Check disk space availability
- Review FileUploadDownloadUtil error logs

## Next Steps

1. Review this document thoroughly
2. Execute database schema updates
3. Add all new Java classes to your project
4. Modify existing pages one by one
5. Test each requirement before moving to next
6. Run data cleanup scripts at the end
