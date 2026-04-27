-- ============================================================
-- ORACLE DATABASE VERIFICATION SCRIPT
-- Advocate Management System (AMS)
-- ============================================================
-- Use this script to verify data persistence in Oracle DB
-- Usage: sqlplus SYSTEM/SYSTEM@XE < verify_data.sql

SET PAGESIZE 50
SET LINESIZE 180
SET FEEDBACK ON
SET ECHO ON

WHENEVER SQLERROR CONTINUE

PROMPT ============================================================
PROMPT  ADVOCATE MANAGEMENT SYSTEM - Data Verification Report
PROMPT ============================================================

-- Check if tables exist
PROMPT
PROMPT 1. CHECKING DATABASE TABLES
PROMPT ─────────────────────────────
SELECT table_name FROM user_tables
WHERE table_name IN ('AMS_USERS1','CASES1','HEARING1','REMINDER1','EVIDENCE1',
                     'COMMUNICATION1','TIMELINE1','ADVOCATE1','JUNIOR_ADVOCATE1',
                     'CLIENT1','STAFF1','ADVOCATE_CASE1','JUNIOR_CASE1',
                     'TASK_ASSIGNMENT1','STRATEGY1','NOTE1','CASE_EVENT1',
                     'TIMELINE1','PAYMENT1','PREFERRED_ADVOCATE1','REPRESENTS1')
ORDER BY table_name;

-- Count records in each main table
PROMPT
PROMPT 2. DATA RECORD COUNTS
PROMPT ──────────────────────

PROMPT AMS_USERS1 Table:
SELECT COUNT(*) as "Total Users" FROM AMS_USERS1;

PROMPT ADVOCATE1 Table:
SELECT COUNT(*) as "Total Advocates" FROM ADVOCATE1;

PROMPT JUNIOR_ADVOCATE1 Table:
SELECT COUNT(*) as "Total Junior Advocates" FROM JUNIOR_ADVOCATE1;

PROMPT CLIENT1 Table:
SELECT COUNT(*) as "Total Clients" FROM CLIENT1;

PROMPT STAFF1 Table:
SELECT COUNT(*) as "Total Staff" FROM STAFF1;

PROMPT CASES1 Table:
SELECT COUNT(*) as "Total Cases" FROM CASES1;

PROMPT HEARING1 Table:
SELECT COUNT(*) as "Total Hearings" FROM HEARING1;

PROMPT REMINDER1 Table:
SELECT COUNT(*) as "Total Reminders" FROM REMINDER1;

PROMPT EVIDENCE1 Table:
SELECT COUNT(*) as "Total Evidence" FROM EVIDENCE1;

PROMPT COMMUNICATION1 Table:
SELECT COUNT(*) as "Total Communications" FROM COMMUNICATION1;

PROMPT TIMELINE1 Table:
SELECT COUNT(*) as "Total Timeline Entries" FROM TIMELINE1;

PROMPT TASK_ASSIGNMENT1 Table:
SELECT COUNT(*) as "Total Tasks" FROM TASK_ASSIGNMENT1;

-- View detailed case data
PROMPT
PROMPT 3. SAMPLE CASES IN DATABASE
PROMPT ─────────────────────────────

COLUMN case_id FORMAT 99999
COLUMN c_title FORMAT A30
COLUMN c_type FORMAT A15
COLUMN status FORMAT A15
COLUMN filed_date FORMAT A12
COLUMN court_name FORMAT A20

SELECT case_id, c_title, c_type, status, TO_CHAR(filed_date,'DD-MON-YYYY') as filed_date, court_name
FROM CASES1
WHERE ROWNUM <= 10
ORDER BY case_id;

-- View detailed hearing data
PROMPT
PROMPT 4. SAMPLE HEARINGS IN DATABASE
PROMPT ──────────────────────────────

COLUMN h_id FORMAT 99999
COLUMN h_date FORMAT A12
COLUMN h_time FORMAT A10
COLUMN court_house FORMAT A20
COLUMN judge_name FORMAT A20

SELECT h_id, case_id, TO_CHAR(h_date,'DD-MON-YYYY') as h_date, h_time, court_house, judge_name, status
FROM HEARING1
WHERE ROWNUM <= 10
ORDER BY h_id;

-- View detailed reminder data
PROMPT
PROMPT 5. SAMPLE REMINDERS IN DATABASE
PROMPT ──────────────────────────────

COLUMN rem_id FORMAT 99999
COLUMN due_date FORMAT A12
COLUMN message FORMAT A40
COLUMN priority FORMAT A10
COLUMN rem_status FORMAT A12

SELECT rem_id, case_id, TO_CHAR(due_date,'DD-MON-YYYY') as due_date, SUBSTR(message,1,40) as message, priority, rem_status
FROM REMINDER1
WHERE ROWNUM <= 10
ORDER BY rem_id;

-- View detailed evidence data
PROMPT
PROMPT 6. SAMPLE EVIDENCE IN DATABASE
PROMPT ──────────────────────────────

COLUMN e_id FORMAT 99999
COLUMN e_type FORMAT A15
COLUMN e_source FORMAT A25
COLUMN collected_date FORMAT A12

SELECT e_id, case_id, e_type, e_source, TO_CHAR(collected_date,'DD-MON-YYYY') as collected_date, admissibility
FROM EVIDENCE1
WHERE ROWNUM <= 10
ORDER BY e_id;

-- View detailed communication data
PROMPT
PROMPT 7. SAMPLE COMMUNICATIONS IN DATABASE
PROMPT ────────────────────────────────────

COLUMN com_id FORMAT 99999
COLUMN comm_mode FORMAT A10
COLUMN init_date FORMAT A12
COLUMN summary FORMAT A40

SELECT com_id, c_id, case_id, comm_mode, TO_CHAR(init_date,'DD-MON-YYYY') as init_date, SUBSTR(summary,1,40) as summary
FROM COMMUNICATION1
WHERE ROWNUM <= 10
ORDER BY com_id;

-- View detailed timeline data
PROMPT
PROMPT 8. SAMPLE TIMELINE ENTRIES IN DATABASE
PROMPT ──────────────────────────────────────

COLUMN time_id FORMAT 99999
COLUMN entry_date FORMAT A12
COLUMN title FORMAT A30

SELECT time_id, case_id, TO_CHAR(entry_date,'DD-MON-YYYY') as entry_date, title, status_indicator
FROM TIMELINE1
WHERE ROWNUM <= 10
ORDER BY time_id;

-- View task assignments
PROMPT
PROMPT 9. SAMPLE TASK ASSIGNMENTS IN DATABASE
PROMPT ──────────────────────────────────────

COLUMN task_id FORMAT 99999
COLUMN task_type FORMAT A20
COLUMN due_date FORMAT A12

SELECT task_id, assigned_by, assigned_to, task_type, TO_CHAR(due_date,'DD-MON-YYYY') as due_date, status
FROM TASK_ASSIGNMENT1
WHERE ROWNUM <= 10
ORDER BY task_id;

-- Verify referential integrity
PROMPT
PROMPT 10. DATA INTEGRITY CHECKS
PROMPT ───────────────────────────

PROMPT Orphaned hearings (case_id not in CASES1):
SELECT h_id, case_id FROM HEARING1
WHERE case_id NOT IN (SELECT case_id FROM CASES1)
AND ROWNUM <= 5;

PROMPT Orphaned reminders (case_id not in CASES1):
SELECT rem_id, case_id FROM REMINDER1
WHERE case_id NOT IN (SELECT case_id FROM CASES1)
AND case_id IS NOT NULL
AND ROWNUM <= 5;

PROMPT Orphaned evidence (case_id not in CASES1):
SELECT e_id, case_id FROM EVIDENCE1
WHERE case_id NOT IN (SELECT case_id FROM CASES1)
AND ROWNUM <= 5;

PROMPT Orphaned task assignments (assigned_to not in JUNIOR_ADVOCATE1):
SELECT task_id, assigned_to FROM TASK_ASSIGNMENT1
WHERE assigned_to NOT IN (SELECT ja_id FROM JUNIOR_ADVOCATE1)
AND ROWNUM <= 5;

-- Count by status
PROMPT
PROMPT 11. DATA DISTRIBUTION BY STATUS
PROMPT ──────────────────────────────

COLUMN status FORMAT A15
COLUMN count FORMAT 9999

PROMPT Cases by Status:
SELECT status, COUNT(*) as count FROM CASES1 GROUP BY status;

PROMPT Hearings by Status:
SELECT status, COUNT(*) as count FROM HEARING1 GROUP BY status;

PROMPT Reminders by Priority:
SELECT priority, COUNT(*) as count FROM REMINDER1 GROUP BY priority;

PROMPT Evidence by Admissibility:
SELECT admissibility, COUNT(*) as count FROM EVIDENCE1 GROUP BY admissibility;

PROMPT Tasks by Status:
SELECT status, COUNT(*) as count FROM TASK_ASSIGNMENT1 GROUP BY status;

-- User accounts summary
PROMPT
PROMPT 12. USER ACCOUNTS SUMMARY
PROMPT ──────────────────────────

COLUMN username FORMAT A25
COLUMN role FORMAT A20

SELECT user_id, username, role, ref_id, is_active FROM AMS_USERS1 ORDER BY role, username;

-- Final summary
PROMPT
PROMPT ============================================================
PROMPT VERIFICATION COMPLETE
PROMPT ============================================================
PROMPT
PROMPT Instructions:
PROMPT 1. Copy this script to: sql/verify_data.sql
PROMPT 2. Connect to database: sqlplus SYSTEM/SYSTEM@XE
PROMPT 3. Run script: @sql/verify_data.sql
PROMPT 4. Review output for data persistence verification
PROMPT
PROMPT If you see data in the tables, persistence is working!
PROMPT All table names use the '1' suffix as defined in schema.sql
PROMPT
PROMPT ============================================================

EXIT;
