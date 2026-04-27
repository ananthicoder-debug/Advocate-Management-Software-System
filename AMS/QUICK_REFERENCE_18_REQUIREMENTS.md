# AMS 18 Requirements - Quick Reference Guide

## Files to Execute First
```
1. sql/schema_updates.sql       → Run in Oracle SQL*Plus
2. sql/data_cleanup.sql        → Run cleanup queries 1-3, 19-21
```

## New Java Files to Add to Project
```
com.ams.util/
  - FileUploadDownloadUtil.java
  - LogCommunicationUtil.java
  - ClientCommunicationUtil.java

com.ams.components/
  - PhotoUploadDialog.java

com.ams.advocate/
  - AddTimelineEventDialog.java

com.ams.login/
  - AddClientDialog.java

com.ams.dashboard/
  - AddSupportStaffDialog.java
```

## Database Tables Created
```
CLIENT_COMMUNICATION      - Client messages with status
MESSAGE_CHAT             - WhatsApp-style messages
EVIDENCE_FILE            - File storage for evidence
CASE_TIMELINE_EVENT      - Timeline events
TASK_DOCUMENT           - Task-related files
TASK_SUBMISSION         - Junior task submissions
LOG_COMMUNICATION       - Communication logs
NOTIFICATION            - Reminder notifications
ADVOCATE_PHOTO          - Advocate photos
JUNIOR_PHOTO            - Junior photos
STAFF_PHOTO             - Staff photos
SUPPORT_STAFF_ROLE      - Staff roles
```

## Updates to Existing Files

| File | Changes Required |
|------|------------------|
| CommunicationsPage | Add status filter combobox, use ClientCommunicationUtil |
| EvidencePage | Add file upload/view buttons, use FileUploadDownloadUtil |
| TimelinePage | Add "+ Add Event" button, open AddTimelineEventDialog |
| JuniorDashboard | Add photo upload button, use PhotoUploadDialog |
| StaffDashboard | Add photo upload, fix duplicate prompt, add staff management |
| ClientsPage | Add "+ Add Client" button, use AddClientDialog |
| JuniorManagePage | Make full-screen, increase form sizes |
| Dashboard header | Add NotificationBellComponent |

## Key Methods to Use

### File Management
```java
FileUploadDownloadUtil.chooseEvidenceFile(parentFrame)
FileUploadDownloadUtil.chooseDocumentFile(parentFrame)
FileUploadDownloadUtil.choosePhotoFile(parentFrame)
FileUploadDownloadUtil.downloadFile(filePath, parentFrame)
FileUploadDownloadUtil.openFile(filePath)
```

### Communication Logging
```java
LogCommunicationUtil.logCommunication(caseId, clientId, advId, 
                                      type, subject, details, notes, followUpDate)
LogCommunicationUtil.getAdvocateLogs(advId, limit)
LogCommunicationUtil.getPendingFollowUps(advId)
```

### Client Communication
```java
ClientCommunicationUtil.storeCommunication(caseId, clientId, advId,
                                          message, mode, subject, filePath)
ClientCommunicationUtil.getCommunications(caseId, statusFilter)
ClientCommunicationUtil.getUnreadCount(advId)
```

## Directory Structure for Files
```
AMS/
├── uploads/
│   ├── evidence/          (images, videos, PDFs)
│   ├── documents/         (task submission files)
│   └── photos/            (profile photos)
├── sql/
├── src/
└── lib/
```

## Implementation Checklist

### Database Phase
- [ ] Run schema_updates.sql
- [ ] Verify all 12 tables created
- [ ] Run data_cleanup.sql queries

### Code Phase
- [ ] Add 3 utility classes
- [ ] Add 4 dialog classes
- [ ] Create NotificationBellComponent
- [ ] Create MessageChatDialog
- [ ] Create LogCommunicationDialog

### UI Updates Phase
- [ ] Update CommunicationsPage
- [ ] Update EvidencePage
- [ ] Update TimelinePage
- [ ] Update JuniorDashboard
- [ ] Update StaffDashboard
- [ ] Update ClientsPage
- [ ] Update JuniorManagePage
- [ ] Update dashboard header

### Testing Phase
- [ ] Test file uploads
- [ ] Test status filtering
- [ ] Test timeline events
- [ ] Test photo uploads
- [ ] Test task submission
- [ ] Test communication logging
- [ ] Test notification bell
- [ ] Test data cleanup
- [ ] Run full system test

## Command Reference

### Running Schema Updates
```bash
sqlplus system/SYSTEM@XE
> @sql/schema_updates.sql
> @sql/data_cleanup.sql
```

### File Upload Configuration
```java
MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
Base upload directory: "uploads"
```

## Database Connection Reference
```java
Connection conn = DBConnection.getConnection();
Sequences: 
  - client_comm_seq
  - message_seq
  - evidence_file_seq
  - timeline_event_seq
  - log_comm_seq
  - notif_seq
```

## Requirement to Implementation Mapping

1. Legal Notes → NOTE1 table (existing)
2. Client Communication → CLIENT_COMMUNICATION + ClientCommunicationUtil
3. Status Filter → ClientCommunicationUtil.getCommunications()
4. Evidence Upload → EVIDENCE_FILE + FileUploadDownloadUtil
5. Timeline Events → CASE_TIMELINE_EVENT + AddTimelineEventDialog
6. Log Communication → LOG_COMMUNICATION + LogCommunicationUtil
7. Notification Bell → NOTIFICATION + NotificationBellComponent
8. Junior Photo → JUNIOR_PHOTO + PhotoUploadDialog
9. Task Submission → TASK_SUBMISSION + FileUploadDownloadUtil
10. Message Chat → MESSAGE_CHAT + MessageChatDialog
11. Staff Profile → STAFF_PHOTO + PhotoUploadDialog
12. Add Junior Screen → Modify JuniorManagePage UI
13. Fix Duplicate Prompt → Fix StaffDashboard
14. Add Client → AddClientDialog
15. Add Support Staff → AddSupportStaffDialog
16. Case Details → Show CaseJourneyDialog
17. Payment Records → Run data_cleanup.sql queries
18. Duplicate Advocates → Run data_cleanup.sql queries

## Common SQL Queries

```sql
-- Check new tables
SELECT table_name FROM user_tables 
WHERE table_name LIKE '%PHOTO' OR table_name LIKE '%MESSAGE%';

-- Check sequences
SELECT sequence_name FROM user_sequences 
WHERE sequence_name LIKE '%SEQ%';

-- Verify data
SELECT COUNT(*) FROM CLIENT_COMMUNICATION;
SELECT COUNT(*) FROM MESSAGE_CHAT;
SELECT COUNT(*) FROM CASE_TIMELINE_EVENT;

-- Check file uploads
SELECT COUNT(*) FROM uploads/photos;
```

## Troubleshooting Quick Fixes

| Issue | Solution |
|-------|----------|
| Database connection fails | Check DBConnection.java defaults |
| File upload not working | Check /uploads directory exists |
| Photos not displaying | Verify PHOTO table populated with path |
| Sequence not found | Run schema_updates.sql again |
| Duplicate error | Run data_cleanup.sql |
| Status filter not working | Check WHERE clause in query |
| Timeline events missing | Verify CASE_TIMELINE_EVENT table |
| Notification not showing | Check NOTIFICATION table populated |

## Support Resources
- IMPLEMENTATION_GUIDE_18_REQUIREMENTS.md
- COMPLETE_SOLUTION_SUMMARY_18_REQUIREMENTS.md
- sql/schema_updates.sql (table definitions)
- sql/data_cleanup.sql (cleanup procedures)

## Success Checklist
- [ ] All 7 new Java files added
- [ ] Schema updates executed
- [ ] Data cleanup completed
- [ ] All 8 existing pages updated
- [ ] Notifications working
- [ ] File uploads working
- [ ] Status filtering working
- [ ] No database errors
- [ ] All tests passing
- [ ] No duplicate data
