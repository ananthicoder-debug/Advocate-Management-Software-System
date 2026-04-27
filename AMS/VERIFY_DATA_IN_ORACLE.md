# QUICK REFERENCE: DATA VERIFICATION IN ORACLE DATABASE

## How to Check if Your Data is Being Saved to Oracle

---

## Method 1: Using SQL*Plus (Simplest)

### Step 1: Open Command Prompt
```
Press Windows Key + R
Type: cmd
Press Enter
```

### Step 2: Connect to Oracle
```cmd
sqlplus SYSTEM/SYSTEM@XE
```

Expected output:
```
SQL*Plus: Release 11.2.0.2.0 Production on ...
Copyright (c) 1991, 2011, ...

Connected to:
Oracle Database 11g Express Edition Release 11.2.0.2.0
```

### Step 3: Run Verification Commands

**Check if tables exist:**
```sql
SELECT table_name FROM user_tables WHERE table_name IN ('CASES','HEARING','REMINDER','EVIDENCE','COMMUNICATION','TIMELINE');
```

Expected: List of 6 table names

---

## Method 2: Quick Data Count

```sql
-- Count all records in main tables
SELECT 'CASES' as table_name, COUNT(*) as record_count FROM CASES
UNION ALL
SELECT 'HEARING', COUNT(*) FROM HEARING
UNION ALL
SELECT 'REMINDER', COUNT(*) FROM REMINDER
UNION ALL
SELECT 'EVIDENCE', COUNT(*) FROM EVIDENCE
UNION ALL
SELECT 'COMMUNICATION', COUNT(*) FROM COMMUNICATION
UNION ALL
SELECT 'TIMELINE', COUNT(*) FROM TIMELINE;
```

---

## Method 3: View Recent Cases (Most Common)

```sql
-- View all cases you added
SELECT case_id, c_title, c_type, status, filed_date FROM CASES ORDER BY case_id DESC;

-- View only new cases (where ID > 1003, original data was 1001-1003)
SELECT case_id, c_title, c_type, status, filed_date FROM CASES WHERE case_id > 1003 ORDER BY case_id;
```

---

## Method 4: View Recent Hearings

```sql
-- View all hearings
SELECT h_id, case_id, h_date, h_time, status FROM HEARING ORDER BY h_id DESC;

-- View only new hearings (where ID > 2003)
SELECT h_id, case_id, h_date, h_time, status FROM HEARING WHERE h_id > 2003;
```

---

## Method 5: View Recent Reminders

```sql
-- View all reminders
SELECT rem_id, case_id, due_date, priority, rem_status FROM REMINDER ORDER BY rem_id DESC;

-- View only new reminders (where ID > 4003)
SELECT rem_id, case_id, due_date, priority, rem_status FROM REMINDER WHERE rem_id > 4003;
```

---

## Method 6: View Recently Added Evidence

```sql
-- View all evidence
SELECT e_id, case_id, e_type, e_source, admissibility FROM EVIDENCE ORDER BY e_id DESC;

-- View only new evidence (where ID > 3003)
SELECT e_id, case_id, e_type, e_source FROM EVIDENCE WHERE e_id > 3003;
```

---

## Method 7: View Communications Log

```sql
-- View all communications
SELECT com_id, c_id, case_id, comm_mode, init_date FROM COMMUNICATION ORDER BY com_id DESC;

-- View only new communications (where ID > 5002)
SELECT com_id, c_id, case_id, comm_mode FROM COMMUNICATION WHERE com_id > 5002;
```

---

## Method 8: Check Relationships

```sql
-- See which advocate handles which cases
SELECT ac.a_id, ac.case_id, cs.c_title 
FROM ADVOCATE_CASE ac 
JOIN CASES cs ON ac.case_id = cs.case_id;
```

---

## Automated Verification Script

### Option A: Use Pre-Built Script
```bash
cd "C:\Users\Admin\Documents\home\AMS"
sqlplus SYSTEM/SYSTEM@XE @sql/verify_data.sql
```

This runs all checks automatically and generates a report.

---

## What to Look For (Signs Data is Saving)

### ✓ Data IS Saving to Oracle:
- COUNT(*) returns increasing numbers after each add
- New case_id values appear in CASES table (e.g., 1004, 1005, etc.)
- New hearing h_id values appear (e.g., 2004, 2005, etc.)
- Query results show your recently added items
- Data persists after closing and reopening application

### ✗ Data is NOT Saving (Check These):
- COUNT(*) stays same after adding items → App running in DEMO MODE
- New items appear in app but not in DB → Using DemoDataStore (session-based)
- Cannot connect to database → Check if Oracle is running on port 1521
- "ORA-" errors in SQL*Plus → Database connection problem

---

## Troubleshooting Queries

### Check if Database is Running:
```sql
SELECT * FROM dual;
```
If you get "ORA-" error, database is not running.

### Check if Oracle User Has Access:
```sql
DESC CASES;
```
If this works, tables are accessible.

### Find Latest Added Records:
```sql
SELECT * FROM CASES WHERE case_id = (SELECT MAX(case_id) FROM CASES);
```

### Count Records by Status:
```sql
SELECT status, COUNT(*) FROM CASES GROUP BY status;
```

---

## Understanding Whether App is in Demo or Production Mode

### How to Tell Which Mode:

**DEMO MODE** (No Database):
- Launch app → No database connection errors
- Add items → They appear in app immediately
- Query database → Items NOT there
- Close/reopen app → Items GONE
- Console: No connection attempt messages (silent)

**PRODUCTION MODE** (With Database):
- Launch app → Successfully connects to Oracle
- Add items → They appear in app
- Query database → Items ARE there in tables
- Close/reopen app → Items STILL there (persistent)
- Console: May show brief connection messages

---

## Complete Data Flow Verification Test

### Test Case: Adding a New Case

**Step 1: Add case in app**
```
1. Launch: java -jar AMS.jar
2. Login (any credentials work in demo or use real user in production)
3. Click "+ Add Case"
4. Enter: Title="Test Case", Type="CIVIL", Court="High Court", Status="ACTIVE"
5. Click "Save Case"
6. Message: "Case added successfully!" ✓
```

**Step 2: Check in Database**
```bash
sqlplus SYSTEM/SYSTEM@XE
```

```sql
-- Find the new case
SELECT * FROM CASES WHERE c_title = 'Test Case';

-- If you see it → DATA IS SAVING TO DATABASE ✓
-- If you don't see it → APP IS IN DEMO MODE (expected without DB)
```

**Step 3: Verify in App Interface**
```
1. Click "Refresh" button on Cases page
2. Should see "Test Case" in the table ✓
```

**Step 4: Verify Persistence**
```bash
# Close and reopen app
exit app
java -jar AMS.jar
```

```
1. Navigate back to Cases page
2. If "Test Case" is still visible → Data persisted ✓
3. If "Test Case" is gone → Running in DEMO MODE (expected)
```

---

## SQL Script Templates

### Save These for Regular Verification:

**Check Everything:**
```sql
SET PAGESIZE 100
SET LINESIZE 200
SELECT 'CASES' as table_name, COUNT(*) as cnt FROM CASES
UNION ALL SELECT 'HEARING', COUNT(*) FROM HEARING
UNION ALL SELECT 'REMINDER', COUNT(*) FROM REMINDER
UNION ALL SELECT 'EVIDENCE', COUNT(*) FROM EVIDENCE
UNION ALL SELECT 'COMMUNICATION', COUNT(*) FROM COMMUNICATION
UNION ALL SELECT 'TIMELINE', COUNT(*) FROM TIMELINE;
```

**View Recent Additions:**
```sql
SELECT case_id, c_title, status FROM CASES WHERE ROWNUM <= 10 ORDER BY case_id DESC;
SELECT h_id, case_id, h_date FROM HEARING WHERE ROWNUM <= 10 ORDER BY h_id DESC;
SELECT rem_id, case_id, due_date FROM REMINDER WHERE ROWNUM <= 10 ORDER BY rem_id DESC;
```

---

## Exit SQL*Plus

```sql
EXIT;
```

---

## Quick Checklist

- [ ] Oracle Database installed on localhost:1521
- [ ] Database SID is XE
- [ ] schema.sql has been run to create tables
- [ ] Can connect with: sqlplus SYSTEM/SYSTEM@XE
- [ ] Application JAR built: AMS.jar
- [ ] Can run: java -jar AMS.jar without errors
- [ ] Can add cases/hearings/etc. in application
- [ ] Can verify data exists in Oracle with SELECT queries
- [ ] Data persists after app restart

---

## Need Help?

If data is not appearing in Oracle:

1. **Check if Oracle is running**
   ```
   sqlplus SYSTEM/SYSTEM@XE
   ```

2. **Check if tables exist**
   ```sql
   SELECT COUNT(*) FROM CASES;
   ```

3. **Check recent activity**
   ```sql
   SELECT * FROM CASES ORDER BY case_id DESC;
   ```

4. **Check application mode**
   - In DEMO MODE: Data in app only, not in DB (expected)
   - In PRODUCTION MODE: Data in both app and DB (desired)

5. **Force production mode**
   - Ensure Oracle is running and accessible
   - Application should auto-detect and use it
   - Verify with: sqlplus SYSTEM/SYSTEM@XE

---

## Success Indicators

You'll know data is saving properly when:

✓ You can see new records in SQL*Plus queries
✓ COUNT(*) increases after adding items in app
✓ Data persists after closing/reopening app
✓ No "ORA-" errors in console
✓ App says "Added successfully!" for each new item
✓ Refresh shows all items (original + new)

*Good luck with your data verification!*
