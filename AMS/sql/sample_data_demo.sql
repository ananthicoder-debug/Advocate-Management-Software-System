-- ============================================================
-- SAMPLE DATA AND QUERIES FOR UPDATED AMS SCHEMA
-- Demonstrating fee billing, payment proofs, staff tracking, and task attachments
-- ============================================================

-- 1. Insert sample staff with fee tracking
INSERT INTO STAFF1 (st_id, st_name, phone, job_title, addr_city, addr_pincode, addr_state, work_hours, emergency_contact, salary, total_fees_collected, last_payment_date, last_payment_mode, payment_notes, dob, join_date, role_type)
VALUES (staff_seq.NEXTVAL, 'Rajesh Kumar', '9876543210', 'Billing Manager', 'Chennai', 600040, 'Tamil Nadu', 8, '9000000001', 50000.00, 0.00, NULL, NULL, NULL, TO_DATE('1985-05-20', 'YYYY-MM-DD'), TO_DATE('2018-03-01', 'YYYY-MM-DD'), 'SUPPORT');

-- 2. Insert sample payment with categories and receipt
INSERT INTO PAYMENT1 (pay_id, case_id, pay_date, amount, pay_category, pay_type, pay_mode, payment_status, bill_no, receipt_path, receipt_type, handled_by, hearing_id, notes)
VALUES (payment_seq.NEXTVAL, 1001, SYSDATE, 15000.00, 'TRAVEL', 'ADVOCATE_FEE', 'ONLINE', 'PAID', 'BILL-2024-001', 'uploads/receipts/receipt_001.jpg', 'IMAGE', 1, NULL, 'Travel expenses for court visit');

-- 3. Insert sample task submission with attachment
INSERT INTO TASK_SUBMISSION (submission_id, task_id, submitted_by, submission_date, submission_time, submission_status, comments, attachment_name, attachment_path, attachment_type, attachment_size, review_date, reviewed_by)
VALUES (submission_seq.NEXTVAL, 5001, 1, SYSDATE, SYSTIMESTAMP, 'SUBMITTED', 'Completed research on case precedents', 'precedent_report.pdf', 'uploads/tasks/task_5001_report.pdf', 'PDF', 2048576, NULL, NULL);

-- 4. Query to view staff fee tracking
SELECT st_name, total_fees_collected, last_payment_date, last_payment_mode, payment_notes
FROM STAFF1
WHERE role_type = 'SUPPORT';

-- 5. Query to view payments with categories and receipts
SELECT p.pay_id, c.c_title AS case_title, p.amount, p.pay_category, p.pay_type, p.pay_mode, p.payment_status, p.receipt_path, s.st_name AS handled_by_staff
FROM PAYMENT1 p
JOIN CASES1 c ON p.case_id = c.case_id
LEFT JOIN STAFF1 s ON p.handled_by = s.st_id
ORDER BY p.pay_date DESC;

-- 6. Query to view task submissions with attachments
SELECT ts.submission_id, ta.task_desc, ja.ja_name AS submitted_by, ts.submission_status, ts.attachment_name, ts.attachment_path, ts.attachment_type, ts.attachment_size, a.a_name AS reviewed_by
FROM TASK_SUBMISSION ts
JOIN TASK_ASSIGNMENT1 ta ON ts.task_id = ta.task_id
JOIN JUNIOR_ADVOCATE1 ja ON ts.submitted_by = ja.ja_id
LEFT JOIN ADVOCATE1 a ON ts.reviewed_by = a.a_id
ORDER BY ts.submission_date DESC;

-- 7. Update payment status to trigger staff fee update
UPDATE PAYMENT1 SET payment_status = 'PAID' WHERE pay_id = 1;

-- 8. View updated staff totals
SELECT st_name, total_fees_collected, last_payment_date, last_payment_mode
FROM STAFF1 WHERE st_id = 1;

COMMIT;