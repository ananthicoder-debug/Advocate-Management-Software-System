-- ============================================================
-- AMS Data Cleanup and Maintenance Queries
-- ============================================================

-- 1. IDENTIFY DUPLICATE ADVOCATES
SELECT a_name, a_id, bar_enroll_no, email, phone, rating, 
       ROW_NUMBER() OVER (PARTITION BY a_name ORDER BY rating DESC, a_id) as rn
FROM ADVOCATE1
ORDER BY a_name, rn;

-- 2. REMOVE DUPLICATE ADVOCATES (Keep highest rated)
DELETE FROM ADVOCATE1 WHERE a_id IN (
    SELECT a_id FROM (
        SELECT a_id, ROW_NUMBER() OVER (PARTITION BY UPPER(a_name) ORDER BY rating DESC, a_id) as rn
        FROM ADVOCATE1
    ) WHERE rn > 1
);

-- 3. REMOVE ORPHANED ADVOCATE_CASE REFERENCES
DELETE FROM ADVOCATE_CASE1 WHERE a_id NOT IN (SELECT a_id FROM ADVOCATE1);

-- 4. REMOVE ORPHANED JUNIOR_CASE REFERENCES  
DELETE FROM JUNIOR_CASE1 WHERE granted_by NOT IN (SELECT a_id FROM ADVOCATE1);

-- 5. REMOVE ORPHANED STRATEGY REFERENCES
DELETE FROM STRATEGY1 WHERE created_by NOT IN (SELECT a_id FROM ADVOCATE1);

-- 6. IDENTIFY DUPLICATE JUNIOR ADVOCATES
SELECT ja_name, ja_id, email, mobile,
       ROW_NUMBER() OVER (PARTITION BY ja_name ORDER BY ja_id) as rn
FROM JUNIOR_ADVOCATE1
ORDER BY ja_name, rn;

-- 7. IDENTIFY DUPLICATE CLIENTS
SELECT c_name, c_id, email, phone,
       ROW_NUMBER() OVER (PARTITION BY c_name ORDER BY c_id) as rn
FROM CLIENT1
ORDER BY c_name, rn;

-- 8. CLEAN UP PAYMENT RECORDS - Remove duplicate entries same case/date
DELETE FROM PAYMENT1 WHERE pay_id IN (
    SELECT pay_id FROM (
        SELECT pay_id, 
               ROW_NUMBER() OVER (PARTITION BY case_id, pay_date ORDER BY pay_id DESC) as rn
        FROM PAYMENT1
    ) WHERE rn > 1
    AND case_id IS NOT NULL
);

-- 9. IDENTIFY INVALID CASES (No client assigned)
SELECT case_id, c_title, c_id FROM CASES1 WHERE c_id IS NULL;

-- 10. IDENTIFY INVALID COMMUNICATIONS (No case assigned)
SELECT * FROM COMMUNICATION1 WHERE case_id IS NULL AND c_id IS NULL;

-- 11. VERIFY NULL VALUES IN REQUIRED FIELDS
SELECT 'ADVOCATE1' as table_name, COUNT(*) as null_names 
FROM ADVOCATE1 WHERE a_name IS NULL
UNION ALL
SELECT 'CLIENT1', COUNT(*) FROM CLIENT1 WHERE c_name IS NULL
UNION ALL
SELECT 'CASES1', COUNT(*) FROM CASES1 WHERE c_title IS NULL;

-- 12. CHECK DATA CONSISTENCY - Advocates in past 30 days
SELECT a_name, email, phone, joined_date, license_status
FROM ADVOCATE1
WHERE license_status = 'ACTIVE'
ORDER BY joined_date DESC;

-- 13. IDENTIFY INACTIVE ADVOCATES (No recent communication)
SELECT a_id, a_name, email
FROM ADVOCATE1
WHERE a_id NOT IN (
    SELECT DISTINCT a_id FROM COMMUNICATION1 
    WHERE a_id IS NOT NULL 
    AND init_date >= TRUNC(SYSDATE) - 30
);

-- 14. REFRESH SEQUENCES (In case of gaps)
-- First check current sequence values:
SELECT sequence_name, last_number FROM user_sequences 
WHERE sequence_name IN ('ADV_SEQ', 'CLIENT_SEQ', 'JA_SEQ', 'STAFF_SEQ', 'CASE_SEQ');

-- 15. STATISTICS - Get overview of data
SELECT 'Advocates' as entity, COUNT(*) as count FROM ADVOCATE1
UNION ALL
SELECT 'Juniors', COUNT(*) FROM JUNIOR_ADVOCATE1
UNION ALL
SELECT 'Clients', COUNT(*) FROM CLIENT1
UNION ALL
SELECT 'Staff', COUNT(*) FROM STAFF1
UNION ALL
SELECT 'Cases', COUNT(*) FROM CASES1
UNION ALL
SELECT 'Communications', COUNT(*) FROM COMMUNICATION1
UNION ALL
SELECT 'Tasks', COUNT(*) FROM TASK_ASSIGNMENT1
UNION ALL
SELECT 'Hearings', COUNT(*) FROM HEARING1;

-- 16. TRANSACTION INTEGRITY CHECK
-- Verify all payments are linked to valid cases
SELECT p.pay_id, p.case_id, p.amount, cs.case_id
FROM PAYMENT1 p
LEFT JOIN CASES1 cs ON p.case_id = cs.case_id
WHERE cs.case_id IS NULL AND p.case_id IS NOT NULL;

-- 17. COMMUNICATION LOG SUMMARY
SELECT 
    comm_mode,
    COUNT(*) as total,
    COUNT(CASE WHEN direction = 'IN' THEN 1 END) as inbound,
    COUNT(CASE WHEN direction = 'OUT' THEN 1 END) as outbound
FROM COMMUNICATION1
WHERE init_date >= TRUNC(SYSDATE) - 30
GROUP BY comm_mode
ORDER BY total DESC;

-- 18. CASE STATUS SUMMARY
SELECT 
    status,
    COUNT(*) as total,
    priority_level
FROM CASES1
GROUP BY status, priority_level
ORDER BY status, priority_level;

-- 19. REMOVE DUPLICATE USERS
-- Keep user with earliest creation date
DELETE FROM AMS_USERS1 WHERE user_id IN (
    SELECT user_id FROM (
        SELECT user_id,
               ROW_NUMBER() OVER (PARTITION BY username ORDER BY created_dt) as rn
        FROM AMS_USERS1
    ) WHERE rn > 1
);

-- 20. VERIFY REFERENTIAL INTEGRITY
-- Check all foreign key relationships
BEGIN
    -- Check ADVOCATE_CASE
    FOR rec IN (SELECT a_id FROM ADVOCATE_CASE1 WHERE a_id NOT IN (SELECT a_id FROM ADVOCATE1)) LOOP
        DELETE FROM ADVOCATE_CASE1 WHERE a_id = rec.a_id;
    END LOOP;
    
    -- Check JUNIOR_CASE
    FOR rec IN (SELECT ja_id FROM JUNIOR_CASE1 WHERE ja_id NOT IN (SELECT ja_id FROM JUNIOR_ADVOCATE1)) LOOP
        DELETE FROM JUNIOR_CASE1 WHERE ja_id = rec.ja_id;
    END LOOP;
    
    -- Check COMMUNICATION
    FOR rec IN (SELECT comm_id FROM COMMUNICATION1 WHERE c_id NOT IN (SELECT c_id FROM CLIENT1)) LOOP
        DELETE FROM COMMUNICATION1 WHERE comm_id = rec.comm_id;
    END LOOP;
    
    COMMIT;
END;
/

-- 21. MARK INACTIVE RECORDS
-- Mark cases closed more than 1 year ago as ARCHIVED
UPDATE CASES1
SET status = 'ARCHIVED'
WHERE status = 'CLOSED'
AND filed_date < TRUNC(SYSDATE) - 365;

COMMIT;

-- 22. SUMMARY REPORT - Run this for audit
SELECT 
    'Database Audit Report' as report_title,
    SYSDATE as report_date,
    (SELECT COUNT(*) FROM ADVOCATE1) as total_advocates,
    (SELECT COUNT(*) FROM JUNIOR_ADVOCATE1) as total_juniors,
    (SELECT COUNT(*) FROM CLIENT1) as total_clients,
    (SELECT COUNT(*) FROM CASES1) as total_cases,
    (SELECT COUNT(*) FROM CASES1 WHERE status = 'ACTIVE') as active_cases,
    (SELECT COUNT(*) FROM COMMUNICATION1 WHERE init_date >= TRUNC(SYSDATE) - 30) as communications_month,
    (SELECT COUNT(*) FROM PAYMENT1 WHERE pay_date >= TRUNC(SYSDATE) - 30) as payments_month
FROM DUAL;
