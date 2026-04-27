# Database Table Name Mapping Reference

## Complete Table Renaming Reference

This document provides a quick reference for all table name changes made during the database reorganization.

### Table Rename Mapping

| Old Name | New Name | Description |
|----------|----------|-------------|
| ADVOCATE | ADVOCATE1 | Senior advocates/lawyers |
| JUNIOR_ADVOCATE | JUNIOR_ADVOCATE1 | Junior advocates/junior lawyers |
| CLIENT | CLIENT1 | Case clients |
| CASES | CASES1 | Legal cases |
| STAFF | STAFF1 | Administrative staff |
| HEARING | HEARING1 | Case hearings |
| EVIDENCE | EVIDENCE1 | Case evidence |
| EVIDENCE_ACCESS | EVIDENCE_ACCESS1 | Evidence access log |
| STRATEGY | STRATEGY1 | Case strategies |
| NOTE | NOTE1 | Case notes |
| COMMUNICATION | COMMUNICATION1 | Client communications |
| CASE_EVENT | CASE_EVENT1 | Case events/milestones |
| TIMELINE | TIMELINE1 | Case timeline |
| REMINDER | REMINDER1 | Task reminders |
| TASK_ASSIGNMENT | TASK_ASSIGNMENT1 | Task assignments |
| PAYMENT | PAYMENT1 | Payment records |
| REPRESENTS | REPRESENTS1 | Advocate-Client relationships |
| PREFERRED_ADVOCATE | PREFERRED_ADVOCATE1 | Client preferred advocates |
| ADVOCATE_CASE | ADVOCATE_CASE1 | Advocate-Case assignments |
| JUNIOR_CASE | JUNIOR_CASE1 | Junior advocate case access |
| AMS_USERS | AMS_USERS1 | User login credentials |

### Foreign Key Relationship Mapping

The following foreign key relationships have been updated:

#### ADVOCATE1 Table
- MENTOR_ID → References ADVOCATE1(A_ID)

#### CASES1 Table
- C_ID → References CLIENT1(C_ID)
- ASSIGNED_ADV → References ADVOCATE1(A_ID)

#### ADVOCATE_CASE1 Table
- A_ID → References ADVOCATE1(A_ID)
- CASE_ID → References CASES1(CASE_ID)

#### JUNIOR_CASE1 Table
- JA_ID → References JUNIOR_ADVOCATE1(JA_ID)
- CASE_ID → References CASES1(CASE_ID)
- GRANTED_BY → References ADVOCATE1(A_ID)

#### HEARING1 Table
- CASE_ID → References CASES1(CASE_ID)

#### EVIDENCE1 Table
- CASE_ID → References CASES1(CASE_ID)
- VERIFIED_BY → References ADVOCATE1(A_ID)

#### EVIDENCE_ACCESS1 Table
- E_ID → References EVIDENCE1(E_ID)

#### STRATEGY1 Table
- CASE_ID → References CASES1(CASE_ID)
- CREATED_BY → References ADVOCATE1(A_ID)

#### NOTE1 Table
- A_ID → References ADVOCATE1(A_ID)
- CASE_ID → References CASES1(CASE_ID)

#### COMMUNICATION1 Table
- C_ID → References CLIENT1(C_ID)
- CASE_ID → References CASES1(CASE_ID)
- A_ID → References ADVOCATE1(A_ID)

#### CASE_EVENT1 Table
- CASE_ID → References CASES1(CASE_ID)

#### TIMELINE1 Table
- CASE_ID → References CASES1(CASE_ID)

#### REMINDER1 Table
- CASE_ID → References CASES1(CASE_ID)

#### TASK_ASSIGNMENT1 Table
- ASSIGNED_BY → References ADVOCATE1(A_ID)
- ASSIGNED_TO → References JUNIOR_ADVOCATE1(JA_ID)
- CASE_ID → References CASES1(CASE_ID)

#### PAYMENT1 Table
- CASE_ID → References CASES1(CASE_ID)
- HEARING_ID → References HEARING1(H_ID)

#### PREFERRED_ADVOCATE1 Table
- CASE_ID → References CASES1(CASE_ID)
- A_ID → References ADVOCATE1(A_ID)

#### REPRESENTS1 Table
- A_ID → References ADVOCATE1(A_ID)
- C_ID → References CLIENT1(C_ID)
- CASE_ID → References CASES1(CASE_ID)

### SQL Update Examples

#### Finding Tables
```sql
-- List all AMS tables
SELECT table_name FROM user_tables 
WHERE table_name LIKE '%1' OR table_name LIKE 'AMS%'
ORDER BY table_name;

-- Count records in each table
SELECT 'ADVOCATE1' as table_name, COUNT(*) as count FROM ADVOCATE1 UNION ALL
SELECT 'CLIENT1', COUNT(*) FROM CLIENT1 UNION ALL
SELECT 'CASES1', COUNT(*) FROM CASES1 UNION ALL
SELECT 'HEARING1', COUNT(*) FROM HEARING1;
```

#### Verifying Foreign Keys
```sql
-- List all constraints
SELECT constraint_name, table_name, r_table_name, r_constraint_name
FROM user_constraints 
WHERE constraint_type = 'R' AND table_name IN 
('ADVOCATE1', 'CLIENT1', 'CASES1', 'HEARING1', 'EVIDENCE1')
ORDER BY table_name;
```

### Java Code Pattern Updates

#### Authentication Query Pattern
**Old:**
```java
"SELECT u.user_id, u.ref_id FROM AMS_USERS u JOIN ADVOCATE a ON u.ref_id=a.a_id"
```

**New:**
```java
"SELECT u.user_id, u.ref_id FROM AMS_USERS1 u JOIN ADVOCATE1 a ON u.ref_id=a.a_id"
```

#### Case Query Pattern
**Old:**
```java
"SELECT cs.case_id FROM CASES cs JOIN CLIENT cl ON cs.c_id=cl.c_id"
```

**New:**
```java
"SELECT cs.case_id FROM CASES1 cs JOIN CLIENT1 cl ON cs.c_id=cl.c_id"
```

#### Hearing Query Pattern
**Old:**
```java
"SELECT h.h_id FROM HEARING h JOIN CASES cs ON h.case_id=cs.case_id"
```

**New:**
```java
"SELECT h.h_id FROM HEARING1 h JOIN CASES1 cs ON h.case_id=cs.case_id"
```

### Sequence Updates

All sequences remain unchanged:
- USER_SEQ - for AMS_USERS1.USER_ID
- ADV_SEQ - for ADVOCATE1.A_ID
- JA_SEQ - for JUNIOR_ADVOCATE1.JA_ID
- CLIENT_SEQ - for CLIENT1.C_ID
- CASE_SEQ - for CASES1.CASE_ID
- STAFF_SEQ - for STAFF1.ST_ID
- HEARING_SEQ - for HEARING1.H_ID
- EVIDENCE_SEQ - for EVIDENCE1.E_ID
- STRATEGY_SEQ - for STRATEGY1.STR_ID
- NOTE_SEQ - for NOTE1.N_ID
- COMM_SEQ - for COMMUNICATION1.COM_ID
- CASE_EVENT_SEQ - for CASE_EVENT1.EV_ID
- TIMELINE_SEQ - for TIMELINE1.TIME_ID
- REMINDER_SEQ - for REMINDER1.REM_ID
- TASK_SEQ - for TASK_ASSIGNMENT1.TASK_ID
- PAYMENT_SEQ - for PAYMENT1.PAY_ID
- PREF_SEQ - for PREFERRED_ADVOCATE1.PREF_ID

### Files Modified Summary

#### Database Files:
- sql/schema.sql (Complete schema with new table names)

#### Java Source Files (26 total):
- com.ams.login package (4 files)
- com.ams.advocate package (10 files)
- com.ams.dashboard package (4 files)
- com.ams.util package (2 files)

### Backward Compatibility Notes

⚠️ **Important:** This is a BREAKING change. The following must be updated:
1. Database backup/restore scripts
2. Database migration scripts
3. Any external tools using Oracle dictionary
4. Documentation and ERD diagrams
5. API documentation if applicable

### Rollback Procedure

If rollback is required:
```sql
-- Rename tables back to original names
ALTER TABLE ADVOCATE1 RENAME TO ADVOCATE;
ALTER TABLE CLIENT1 RENAME TO CLIENT;
ALTER TABLE CASES1 RENAME TO CASES;
-- ... repeat for all 21 tables
```

Then revert Java source code to use original table names.

---

**Document Version:** 1.0  
**Last Updated:** Current Session  
**Status:** Complete  
**Ready for:** Testing & Validation
