-- ============================================================
-- ADVOCATE MANAGEMENT SYSTEM - Schema Updates
-- Adding missing tables for new features
-- ============================================================

-- 1. CLIENT COMMUNICATION TABLE (enhanced with status)
CREATE TABLE CLIENT_COMMUNICATION (
    comm_id        NUMBER PRIMARY KEY,
    case_id        NUMBER REFERENCES CASES1(case_id),
    c_id           NUMBER REFERENCES CLIENT1(c_id),
    a_id           NUMBER REFERENCES ADVOCATE1(a_id),
    message_text   CLOB NOT NULL,
    sent_date      DATE DEFAULT SYSDATE,
    comm_status    VARCHAR2(30) DEFAULT 'SENT', -- SENT/RECEIVED/READ/PENDING
    comm_mode      VARCHAR2(30), -- CALL/EMAIL/MEETING/MESSAGE/CHAT
    direction      VARCHAR2(10), -- IN/OUT
    subject        VARCHAR2(200),
    file_attached  VARCHAR2(1) DEFAULT 'N', -- Y/N
    file_path      VARCHAR2(500)
);

CREATE SEQUENCE client_comm_seq START WITH 1 INCREMENT BY 1;

-- 2. MESSAGE CHAT TABLE (for WhatsApp-style messaging)
CREATE TABLE MESSAGE_CHAT (
    msg_id         NUMBER PRIMARY KEY,
    sender_id      NUMBER,
    receiver_id    NUMBER,
    sender_type    VARCHAR2(20), -- ADVOCATE/CLIENT/JUNIOR
    message_text   CLOB NOT NULL,
    sent_date      TIMESTAMP DEFAULT SYSTIMESTAMP,
    read_status    VARCHAR2(20) DEFAULT 'UNREAD', -- UNREAD/READ
    msg_type       VARCHAR2(20) DEFAULT 'TEXT', -- TEXT/IMAGE/FILE/VOICE
    media_path     VARCHAR2(500),
    case_id        NUMBER REFERENCES CASES1(case_id)
);

CREATE SEQUENCE message_seq START WITH 1 INCREMENT BY 1;

-- 3. EVIDENCE FILES TABLE (for storing file uploads)
CREATE TABLE EVIDENCE_FILE (
    file_id        NUMBER PRIMARY KEY,
    e_id           NUMBER REFERENCES EVIDENCE1(e_id),
    file_name      VARCHAR2(200) NOT NULL,
    file_path      VARCHAR2(500) NOT NULL,
    file_type      VARCHAR2(20), -- IMAGE/VIDEO/PDF/DOCUMENT
    file_size      NUMBER,
    mime_type      VARCHAR2(50),
    uploaded_date  DATE DEFAULT SYSDATE,
    uploaded_by    NUMBER,
    description    VARCHAR2(500)
);

CREATE SEQUENCE evidence_file_seq START WITH 1 INCREMENT BY 1;

-- 4. CASE TIMELINE EVENTS TABLE (enhanced for add event feature)
CREATE TABLE CASE_TIMELINE_EVENT (
    timeline_event_id NUMBER PRIMARY KEY,
    case_id       NUMBER REFERENCES CASES1(case_id),
    event_title   VARCHAR2(200) NOT NULL,
    event_date    DATE NOT NULL,
    event_time    VARCHAR2(10),
    event_description CLOB,
    event_type    VARCHAR2(50), -- HEARING/FILING/MEETING/SUBMISSION/OTHER
    created_by    NUMBER,
    created_date  DATE DEFAULT SYSDATE,
    modified_date DATE,
    event_status  VARCHAR2(20) DEFAULT 'SCHEDULED' -- SCHEDULED/COMPLETED/CANCELLED
);

CREATE SEQUENCE timeline_event_seq START WITH 1 INCREMENT BY 1;

-- 5. TASK DOCUMENT TABLE (for storing task-related documents)
CREATE TABLE TASK_DOCUMENT (
    task_doc_id   NUMBER PRIMARY KEY,
    task_id       NUMBER REFERENCES TASK_ASSIGNMENT1(task_id),
    doc_name      VARCHAR2(200) NOT NULL,
    doc_path      VARCHAR2(500) NOT NULL,
    doc_type      VARCHAR2(50),
    uploaded_date DATE DEFAULT SYSDATE,
    uploaded_by   NUMBER,
    description   VARCHAR2(500)
);

CREATE SEQUENCE task_doc_seq START WITH 1 INCREMENT BY 1;

-- 6. TASK SUBMISSION TABLE (for junior task submission tracking)
CREATE TABLE TASK_SUBMISSION (
    submission_id    NUMBER PRIMARY KEY,
    task_id          NUMBER REFERENCES TASK_ASSIGNMENT1(task_id),
    submitted_by     NUMBER REFERENCES JUNIOR_ADVOCATE1(ja_id),
    submission_date  DATE DEFAULT SYSDATE,
    submission_time  TIMESTAMP DEFAULT SYSTIMESTAMP,
    submission_status VARCHAR2(20) DEFAULT 'SUBMITTED' CHECK (submission_status IN ('SUBMITTED','REVIEWED','APPROVED','REJECTED')),
    comments         CLOB,
    attachment_name  VARCHAR2(200),
    attachment_path  VARCHAR2(500),
    attachment_type  VARCHAR2(50),
    attachment_size  NUMBER,
    review_date      DATE,
    reviewed_by      NUMBER REFERENCES ADVOCATE1(a_id)
);

CREATE SEQUENCE submission_seq START WITH 1 INCREMENT BY 1;

-- 7. LOG COMMUNICATION TABLE (detailed log of all communications)
CREATE TABLE LOG_COMMUNICATION (
    log_comm_id    NUMBER PRIMARY KEY,
    case_id        NUMBER REFERENCES CASES1(case_id),
    c_id           NUMBER REFERENCES CLIENT1(c_id),
    a_id           NUMBER REFERENCES ADVOCATE1(a_id),
    log_type       VARCHAR2(30), -- CALL/EMAIL/MESSAGE/MEETING
    log_date       DATE DEFAULT SYSDATE,
    log_time       TIMESTAMP DEFAULT SYSTIMESTAMP,
    log_subject    VARCHAR2(200),
    log_details    CLOB,
    log_duration   NUMBER, -- in minutes
    log_notes      VARCHAR2(500),
    follow_up_date DATE,
    follow_up_status VARCHAR2(20) DEFAULT 'PENDING'
);

CREATE SEQUENCE log_comm_seq START WITH 1 INCREMENT BY 1;

-- 8. NOTIFICATION TABLE (for bell icon notifications)
CREATE TABLE NOTIFICATION (
    notif_id       NUMBER PRIMARY KEY,
    user_id        NUMBER,
    user_role      VARCHAR2(20),
    notif_type     VARCHAR2(30), -- REMINDER/MESSAGE/TASK/ALERT
    notif_title    VARCHAR2(200),
    notif_message  CLOB,
    notif_date     DATE DEFAULT SYSDATE,
    notif_time     TIMESTAMP DEFAULT SYSTIMESTAMP,
    read_status    VARCHAR2(20) DEFAULT 'UNREAD',
    related_id     NUMBER, -- reminder_id, task_id, etc.
    related_type   VARCHAR2(30), -- REMINDER, TASK, MESSAGE, etc.
    priority       VARCHAR2(20) DEFAULT 'NORMAL' -- HIGH/NORMAL/LOW
);

CREATE SEQUENCE notif_seq START WITH 1 INCREMENT BY 1;

-- 9. ADVOCATE PROFILE PHOTO TABLE (for managing advocate photos)
CREATE TABLE ADVOCATE_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    a_id           NUMBER REFERENCES ADVOCATE1(a_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE adv_photo_seq START WITH 1 INCREMENT BY 1;

-- 10. JUNIOR ADVOCATE PHOTO TABLE
CREATE TABLE JUNIOR_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    ja_id          NUMBER REFERENCES JUNIOR_ADVOCATE1(ja_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE junior_photo_seq START WITH 1 INCREMENT BY 1;

-- 11. STAFF PHOTO TABLE
CREATE TABLE STAFF_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    st_id          NUMBER REFERENCES STAFF1(st_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE staff_photo_seq START WITH 1 INCREMENT BY 1;

-- 12. ENHANCE EVIDENCE TABLE WITH FILE STORAGE
ALTER TABLE EVIDENCE1 ADD (file_upload_date DATE, file_uploaded_by NUMBER);

-- 13. MODIFY COMMUNICATION TABLE TO ADD STATUS
ALTER TABLE COMMUNICATION1 ADD (comm_status VARCHAR2(30) DEFAULT 'PENDING');

-- 14. ADD SUPPORT STAFF MANAGEMENT ENHANCEMENTS
CREATE TABLE SUPPORT_STAFF_ROLE (
    role_id        NUMBER PRIMARY KEY,
    st_id          NUMBER REFERENCES STAFF1(st_id),
    department     VARCHAR2(50),
    responsibilities VARCHAR2(500),
    assigned_date  DATE DEFAULT SYSDATE
);

CREATE SEQUENCE support_role_seq START WITH 1 INCREMENT BY 1;

-- ALTER existing tables to support fee billing and task attachments
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STAFF1 ADD (total_fees_collected NUMBER(12,2) DEFAULT 0 CHECK (total_fees_collected >= 0), last_payment_date DATE, last_payment_mode VARCHAR2(20), payment_notes VARCHAR2(500))';
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE PAYMENT1 ADD (pay_category VARCHAR2(50) DEFAULT ''GENERAL'' CHECK (pay_category IN (''TRAVEL'',''HEARING'',''MONTHLY_FEE'',''CONSULTATION'',''OTHER'')), payment_status VARCHAR2(20) DEFAULT ''PENDING'' CHECK (payment_status IN (''PENDING'',''PAID'',''REJECTED'')), receipt_path VARCHAR2(500), receipt_type VARCHAR2(50), handled_by NUMBER)';
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE PAYMENT1 ADD CONSTRAINT FK_PAYMENT1_STAFF1_HANDLED_BY FOREIGN KEY (handled_by) REFERENCES STAFF1(st_id)';
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE TASK_SUBMISSION ADD (attachment_name VARCHAR2(200), attachment_path VARCHAR2(500), attachment_type VARCHAR2(50), attachment_size NUMBER)';
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

-- SYNONYMS FOR LEGACY OBJECT NAMES
CREATE SYNONYM CASE_EVENT FOR CASE_EVENT1;
CREATE SYNONYM EVIDENCE FOR EVIDENCE1;
CREATE SYNONYM COMMUNICATION FOR COMMUNICATION1;

COMMIT;
