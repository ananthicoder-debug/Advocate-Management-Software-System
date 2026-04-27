-- ============================================================
-- ADVOCATE MANAGEMENT SYSTEM - Oracle SQL Schema
-- ============================================================

-- USERS TABLE (for login)
CREATE TABLE AMS_USERS1 (
    user_id    NUMBER PRIMARY KEY,
    username   VARCHAR2(25) NOT NULL UNIQUE,
    password   VARCHAR2(100) NOT NULL,
    role       VARCHAR2(20) NOT NULL CHECK (role IN ('SENIOR_ADVOCATE','JUNIOR_ADVOCATE','CLIENT','STAFF')),
    ref_id     NUMBER,
    is_active  NUMBER(1) DEFAULT 1 CHECK (is_active IN (0,1)),
    created_dt DATE DEFAULT SYSDATE
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;

-- ADVOCATE TABLE
CREATE TABLE ADVOCATE1 (
    a_id           NUMBER PRIMARY KEY,
    a_name         VARCHAR2(25) NOT NULL,
    bar_enroll_no  VARCHAR2(30) UNIQUE,
    email          VARCHAR2(100),
    phone          VARCHAR2(15),
    license_status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (license_status IN ('ACTIVE','SUSPENDED','REVOKED')),
    office_room    VARCHAR2(10),
    joined_date    DATE,
    addr_city      VARCHAR2(30),
    addr_street    VARCHAR2(30),
    addr_pincode   NUMBER(6),
    expert_at      VARCHAR2(30),
    yoe            NUMBER(3) DEFAULT 0 CHECK (yoe >= 0 AND yoe <= 99),
    yob            NUMBER(4) CHECK (yob BETWEEN 1900 AND EXTRACT(YEAR FROM SYSDATE)+1),
    profile_notes  VARCHAR2(500),
    rating         NUMBER(3,2) DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    photo_path     VARCHAR2(200)
);

CREATE SEQUENCE adv_seq START WITH 1 INCREMENT BY 1;

-- JUNIOR ADVOCATE TABLE
CREATE TABLE JUNIOR_ADVOCATE1 (
    ja_id          NUMBER PRIMARY KEY,
    ja_name        VARCHAR2(30) NOT NULL,
    email          VARCHAR2(100),
    mobile         VARCHAR2(15),
    qualification  VARCHAR2(30),
    joined_date    DATE,
    department     VARCHAR2(30),
    mentor_id      NUMBER REFERENCES ADVOCATE1(a_id),
    work_status    VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (work_status IN ('ACTIVE','INACTIVE','ON_LEAVE')),
    desk_location  VARCHAR2(20),
    rating         NUMBER(3,2) DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    photo_path     VARCHAR2(200)
);

CREATE SEQUENCE ja_seq START WITH 1 INCREMENT BY 1;

-- STAFF TABLE
CREATE TABLE STAFF1 (
    st_id            NUMBER PRIMARY KEY,
    st_name          VARCHAR2(100) NOT NULL,
    phone            VARCHAR2(15),
    job_title        VARCHAR2(50),
    addr_city        VARCHAR2(50),
    addr_pincode     NUMBER,
    addr_state       VARCHAR2(30),
    work_hours       NUMBER CHECK (work_hours >= 0 AND work_hours <= 24),
    emergency_contact VARCHAR2(15),
    salary           NUMBER(10,2) CHECK (salary >= 0),
    total_fees_collected NUMBER(12,2) DEFAULT 0 CHECK (total_fees_collected >= 0),
    last_payment_date DATE,
    last_payment_mode VARCHAR2(20),
    payment_notes    VARCHAR2(500),
    dob              DATE,
    join_date        DATE,
    role_type        VARCHAR2(20) DEFAULT 'ADMIN' CHECK (role_type IN ('ADMIN','SUPPORT'))
);

CREATE SEQUENCE staff_seq START WITH 1 INCREMENT BY 1;

-- CLIENT TABLE
CREATE TABLE CLIENT1 (
    c_id              NUMBER PRIMARY KEY,
    c_name            VARCHAR2(30) NOT NULL,
    email             VARCHAR2(100),
    phone             VARCHAR2(15),
    nat_id            VARCHAR2(30),
    cl_type           VARCHAR2(20) CHECK (cl_type IN ('INDIVIDUAL','CORPORATE')),
    addr_city         VARCHAR2(30),
    addr_street       VARCHAR2(30),
    addr_pincode      NUMBER(6),
    first_contact_dt  DATE DEFAULT SYSDATE,
    comm_notes        VARCHAR2(500),
    username          VARCHAR2(30),
    password_hash     VARCHAR2(100),
    photo_data        BLOB,
    rating_given      NUMBER(3,2) DEFAULT 0 CHECK (rating_given BETWEEN 0 AND 5)
);

CREATE SEQUENCE client_seq START WITH 1 INCREMENT BY 1;

-- CASES TABLE
CREATE TABLE CASES1 (
    case_id        NUMBER PRIMARY KEY,
    c_id           NUMBER NOT NULL REFERENCES CLIENT1(c_id),
    c_title        VARCHAR2(100) NOT NULL,
    c_type         VARCHAR2(30) CHECK (c_type IN ('CIVIL','CRIMINAL','CORPORATE','FAMILY','OTHER')),
    law_category   VARCHAR2(50),
    problem_desc   CLOB,
    status         VARCHAR2(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACTIVE','CLOSED','ARCHIVED')),
    priority_level NUMBER(1) DEFAULT 2 CHECK (priority_level IN (1,2,3)),
    filed_date     DATE DEFAULT SYSDATE,
    court_name     VARCHAR2(100),
    fee_amount     NUMBER(10,2) DEFAULT 0 CHECK (fee_amount >= 0),
    total_fees     NUMBER(10,2) DEFAULT 0 CHECK (total_fees >= 0),
    ca_name        VARCHAR2(50),
    log_title      VARCHAR2(100),
    assigned_adv   NUMBER REFERENCES ADVOCATE1(a_id)
);

CREATE SEQUENCE case_seq START WITH 1 INCREMENT BY 1;

-- ADVOCATE_CASE (handles) TABLE
CREATE TABLE ADVOCATE_CASE1 (
    a_id    NUMBER REFERENCES ADVOCATE1(a_id),
    case_id NUMBER REFERENCES CASES1(case_id),
    PRIMARY KEY (a_id, case_id)
);

-- JUNIOR_CASE (junior access to case)
CREATE TABLE JUNIOR_CASE1 (
    ja_id   NUMBER REFERENCES JUNIOR_ADVOCATE1(ja_id),
    case_id NUMBER REFERENCES CASES1(case_id),
    granted_by NUMBER REFERENCES ADVOCATE1(a_id),
    granted_dt DATE DEFAULT SYSDATE,
    PRIMARY KEY (ja_id, case_id)
);

-- HEARING TABLE
CREATE TABLE HEARING1 (
    h_id         NUMBER PRIMARY KEY,
    case_id      NUMBER NOT NULL REFERENCES CASES1(case_id),
    h_date       DATE NOT NULL,
    h_time       VARCHAR2(10),
    court_house  VARCHAR2(50),
    room_no      VARCHAR2(15),
    purpose      VARCHAR2(100),
    doc_required VARCHAR2(500),
    judge_name   VARCHAR2(50),
    co_time      VARCHAR2(20),
    outcome      VARCHAR2(200),
    status       VARCHAR2(20) DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING','COMPLETED','ADJOURNED'))
);

CREATE SEQUENCE hearing_seq START WITH 1 INCREMENT BY 1;

-- EVIDENCE TABLE
CREATE TABLE EVIDENCE1 (
    e_id           NUMBER PRIMARY KEY,
    case_id        NUMBER NOT NULL REFERENCES CASES1(case_id),
    e_type         VARCHAR2(30) DEFAULT 'OTHER' CHECK (e_type IN ('DOCUMENT','IMAGE','VIDEO','OTHER')),
    e_source       VARCHAR2(100),
    e_desc         CLOB,
    collected_date DATE DEFAULT SYSDATE,
    admissibility  VARCHAR2(20) DEFAULT 'PENDING' CHECK (admissibility IN ('PENDING','ADMISSIBLE','REJECTED')),
    verified_by    NUMBER REFERENCES ADVOCATE1(a_id),
    e_location     VARCHAR2(100),
    file_path      VARCHAR2(300) NOT NULL,
    submitted_court NUMBER(1) DEFAULT 0 CHECK (submitted_court IN (0,1))
);

CREATE SEQUENCE evidence_seq START WITH 1 INCREMENT BY 1;

-- EVIDENCE ACCESS (security log)
CREATE TABLE EVIDENCE_ACCESS1 (
    log_id       NUMBER PRIMARY KEY,
    e_id         NUMBER NOT NULL REFERENCES EVIDENCE1(e_id),
    accessed_by  NUMBER,
    access_role  VARCHAR2(30),
    access_time  TIMESTAMP DEFAULT SYSTIMESTAMP,
    reason       VARCHAR2(200),
    action_type  VARCHAR2(30)
);

CREATE SEQUENCE evlog_seq START WITH 1 INCREMENT BY 1;

-- STRATEGY TABLE
CREATE TABLE STRATEGY1 (
    str_id       NUMBER PRIMARY KEY,
    case_id      NUMBER NOT NULL REFERENCES CASES1(case_id),
    heading      VARCHAR2(200) NOT NULL,
    law_section  VARCHAR2(200),
    co_argument  VARCHAR2(500),
    arg_text     CLOB,
    notes        VARCHAR2(500),
    status       VARCHAR2(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','FINAL','REVIEW','ARCHIVED')),
    created_by   NUMBER REFERENCES ADVOCATE1(a_id),
    created_dt   DATE DEFAULT SYSDATE
);

CREATE SEQUENCE strategy_seq START WITH 1 INCREMENT BY 1;

-- NOTE TABLE
CREATE TABLE NOTE1 (
    n_id            NUMBER PRIMARY KEY,
    a_id            NUMBER REFERENCES ADVOCATE1(a_id),
    case_id         NUMBER REFERENCES CASES1(case_id),
    analysis        CLOB,
    en_data         DATE DEFAULT SYSDATE,
    followup_q      VARCHAR2(500),
    imp_judgement   VARCHAR2(500),
    con_status      VARCHAR2(30) DEFAULT 'OPEN' CHECK (con_status IN ('OPEN','IN_PROGRESS','CLOSED'))
);

CREATE SEQUENCE note_seq START WITH 1 INCREMENT BY 1;

-- COMMUNICATION TABLE
CREATE TABLE COMMUNICATION1 (
    com_id        NUMBER PRIMARY KEY,
    c_id          NUMBER REFERENCES CLIENT1(c_id),
    case_id       NUMBER REFERENCES CASES1(case_id),
    a_id          NUMBER REFERENCES ADVOCATE1(a_id),
    init_date     DATE DEFAULT SYSDATE,
    comm_mode     VARCHAR2(30) DEFAULT 'MESSAGE' CHECK (comm_mode IN ('CALL','EMAIL','MEETING','MESSAGE','CHAT')),
    duration      NUMBER CHECK (duration >= 0),
    summary       CLOB,
    follow_action VARCHAR2(500),
    direction     VARCHAR2(10) DEFAULT 'OUT' CHECK (direction IN ('IN','OUT')),
    comm_status   VARCHAR2(30) DEFAULT 'PENDING'
);

CREATE SEQUENCE comm_seq START WITH 1 INCREMENT BY 1;

-- CASE EVENT TABLE
CREATE TABLE CASE_EVENT1 (
    ev_id        NUMBER PRIMARY KEY,
    case_id      NUMBER NOT NULL REFERENCES CASES1(case_id),
    event_date   DATE DEFAULT SYSDATE,
    title        VARCHAR2(200) NOT NULL,
    description  CLOB,
    location     VARCHAR2(200),
    attended_by  VARCHAR2(200),
    outcome      VARCHAR2(500),
    task_status  VARCHAR2(20) DEFAULT 'PENDING' CHECK (task_status IN ('PENDING','COMPLETED','DELAYED','CANCELLED')),
    is_important NUMBER(1) DEFAULT 0 CHECK (is_important IN (0,1))
);

CREATE SEQUENCE caseevent_seq START WITH 1 INCREMENT BY 1;

-- TIMELINE TABLE
CREATE TABLE TIMELINE1 (
    time_id          NUMBER PRIMARY KEY,
    case_id          NUMBER NOT NULL REFERENCES CASES1(case_id),
    entry_date       DATE DEFAULT SYSDATE,
    title            VARCHAR2(200) NOT NULL,
    e_data           CLOB,
    progress_sum     VARCHAR2(500),
    next_step        VARCHAR2(300),
    status_indicator VARCHAR2(30) DEFAULT 'ON_TRACK' CHECK (status_indicator IN ('ON_TRACK','DELAYED','COMPLETED')),
    pro_par          NUMBER(3,2) DEFAULT 0 CHECK (pro_par >= 0)
);

CREATE SEQUENCE timeline_seq START WITH 1 INCREMENT BY 1;

-- REMINDER TABLE
CREATE TABLE REMINDER1 (
    rem_id          NUMBER PRIMARY KEY,
    a_id            NUMBER NOT NULL REFERENCES ADVOCATE1(a_id),
    case_id         NUMBER NOT NULL REFERENCES CASES1(case_id),
    due_date        DATE NOT NULL,
    message         CLOB NOT NULL,
    priority        VARCHAR2(20) DEFAULT 'MEDIUM' CHECK (priority IN ('HIGH','MEDIUM','LOW')),
    rem_status      VARCHAR2(20) DEFAULT 'PENDING' CHECK (rem_status IN ('PENDING','COMPLETED','CANCELLED')),
    created_date    DATE DEFAULT SYSDATE,
    allot_frequency VARCHAR2(30)
);

CREATE SEQUENCE reminder_seq START WITH 1 INCREMENT BY 1;

-- TASK ASSIGNMENT (Senior to Junior)
CREATE TABLE TASK_ASSIGNMENT1 (
    task_id       NUMBER PRIMARY KEY,
    assigned_by   NUMBER NOT NULL REFERENCES ADVOCATE1(a_id),
    assigned_to   NUMBER NOT NULL REFERENCES JUNIOR_ADVOCATE1(ja_id),
    case_id       NUMBER NOT NULL REFERENCES CASES1(case_id),
    task_desc     CLOB NOT NULL,
    task_type     VARCHAR2(50),
    due_date      DATE,
    status        VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','REJECTED')),
    submitted_file VARCHAR2(300),
    rating        NUMBER(1) CHECK (rating BETWEEN 0 AND 5),
    assigned_dt   DATE DEFAULT SYSDATE,
    completed_dt  DATE,
    feedback      VARCHAR2(500)
);

CREATE SEQUENCE task_seq START WITH 1 INCREMENT BY 1;

-- PAYMENT TABLE
CREATE TABLE PAYMENT1 (
    pay_id          NUMBER PRIMARY KEY,
    case_id         NUMBER NOT NULL REFERENCES CASES1(case_id),
    pay_date        DATE DEFAULT SYSDATE,
    amount          NUMBER(12,2) DEFAULT 0 CHECK (amount >= 0),
    pay_category    VARCHAR2(50) DEFAULT 'GENERAL' CHECK (pay_category IN ('TRAVEL','HEARING','MONTHLY_FEE','CONSULTATION','OTHER')),
    pay_type        VARCHAR2(30) DEFAULT 'FEE' CHECK (pay_type IN ('FEE','TRAVEL','MISC','ADVOCATE_FEE','SERVICE')),
    pay_mode        VARCHAR2(20) CHECK (pay_mode IN ('CASH','CHEQUE','ONLINE','CARD','UPI','OTHER')),
    payment_status  VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PAID','REJECTED')),
    bill_no         VARCHAR2(30),
    receipt_path    VARCHAR2(500),
    receipt_type    VARCHAR2(50),
    handled_by      NUMBER REFERENCES STAFF1(st_id),
    hearing_id      NUMBER REFERENCES HEARING1(h_id),
    notes           VARCHAR2(500)
);

CREATE SEQUENCE payment_seq START WITH 1 INCREMENT BY 1;

-- PREFERRED ADVOCATE (Client's preference)
CREATE TABLE PREFERRED_ADVOCATE1 (
    pref_id   NUMBER PRIMARY KEY,
    case_id   NUMBER NOT NULL REFERENCES CASES1(case_id),
    a_id      NUMBER NOT NULL REFERENCES ADVOCATE1(a_id),
    pref_rank NUMBER(1) CHECK (pref_rank IN (1,2,3)),
    status    VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACCEPTED','REJECTED'))
);

CREATE SEQUENCE pref_seq START WITH 1 INCREMENT BY 1;

-- REPRESENTS
CREATE TABLE REPRESENTS1 (
    a_id           NUMBER NOT NULL REFERENCES ADVOCATE1(a_id),
    c_id           NUMBER NOT NULL REFERENCES CLIENT1(c_id),
    case_id        NUMBER NOT NULL REFERENCES CASES1(case_id),
    rep_start_date DATE DEFAULT SYSDATE,
    fee_agg_type   VARCHAR2(30),
    engae_status   VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (engae_status IN ('ACTIVE','INACTIVE')),
    PRIMARY KEY (a_id, c_id, case_id)
);
-- CLIENT COMMUNICATION TABLE
CREATE TABLE CLIENT_COMMUNICATION (
    comm_id        NUMBER PRIMARY KEY,
    case_id        NUMBER REFERENCES CASES1(case_id),
    c_id           NUMBER REFERENCES CLIENT1(c_id),
    a_id           NUMBER REFERENCES ADVOCATE1(a_id),
    message_text   CLOB NOT NULL,
    sent_date      DATE DEFAULT SYSDATE,
    comm_status    VARCHAR2(30) DEFAULT 'SENT',
    comm_mode      VARCHAR2(30),
    direction      VARCHAR2(10),
    subject        VARCHAR2(200),
    file_attached  VARCHAR2(1) DEFAULT 'N',
    file_path      VARCHAR2(500)
);

CREATE SEQUENCE client_comm_seq START WITH 1 INCREMENT BY 1;

-- LOG COMMUNICATION TABLE
CREATE TABLE LOG_COMMUNICATION (
    log_comm_id     NUMBER PRIMARY KEY,
    case_id         NUMBER REFERENCES CASES1(case_id),
    c_id            NUMBER REFERENCES CLIENT1(c_id),
    a_id            NUMBER REFERENCES ADVOCATE1(a_id),
    log_type        VARCHAR2(30),
    log_date        DATE DEFAULT SYSDATE,
    log_time        TIMESTAMP DEFAULT SYSTIMESTAMP,
    log_subject     VARCHAR2(200),
    log_details     CLOB,
    log_duration    NUMBER,
    log_notes       VARCHAR2(500),
    follow_up_date  DATE,
    follow_up_status VARCHAR2(20) DEFAULT 'PENDING'
);

CREATE SEQUENCE log_comm_seq START WITH 1 INCREMENT BY 1;

-- NOTIFICATION TABLE
CREATE TABLE NOTIFICATION (
    notif_id       NUMBER PRIMARY KEY,
    user_id        NUMBER,
    user_role      VARCHAR2(20),
    notif_type     VARCHAR2(30),
    notif_title    VARCHAR2(200),
    notif_message  CLOB,
    notif_date     DATE DEFAULT SYSDATE,
    notif_time     TIMESTAMP DEFAULT SYSTIMESTAMP,
    read_status    VARCHAR2(20) DEFAULT 'UNREAD',
    related_id     NUMBER,
    related_type   VARCHAR2(30),
    priority       VARCHAR2(20) DEFAULT 'NORMAL'
);

CREATE SEQUENCE notif_seq START WITH 1 INCREMENT BY 1;

-- MESSAGE CHAT TABLE
CREATE TABLE MESSAGE_CHAT (
    msg_id         NUMBER PRIMARY KEY,
    sender_id      NUMBER,
    receiver_id    NUMBER,
    sender_type    VARCHAR2(20),
    message_text   CLOB NOT NULL,
    sent_date      TIMESTAMP DEFAULT SYSTIMESTAMP,
    read_status    VARCHAR2(20) DEFAULT 'UNREAD',
    msg_type       VARCHAR2(20) DEFAULT 'TEXT',
    media_path     VARCHAR2(500),
    case_id        NUMBER REFERENCES CASES1(case_id)
);

CREATE SEQUENCE message_seq START WITH 1 INCREMENT BY 1;

-- EVIDENCE FILE TABLE
CREATE TABLE EVIDENCE_FILE (
    file_id        NUMBER PRIMARY KEY,
    e_id           NUMBER REFERENCES EVIDENCE1(e_id),
    file_name      VARCHAR2(200) NOT NULL,
    file_path      VARCHAR2(500) NOT NULL,
    file_type      VARCHAR2(20),
    file_size      NUMBER,
    mime_type      VARCHAR2(50),
    uploaded_date  DATE DEFAULT SYSDATE,
    uploaded_by    NUMBER,
    description    VARCHAR2(500)
);

CREATE SEQUENCE evidence_file_seq START WITH 1 INCREMENT BY 1;

-- TASK DOCUMENT TABLE
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

-- TASK SUBMISSION TABLE
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

-- ADVOCATE PROFILE PHOTO TABLE
CREATE TABLE ADVOCATE_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    a_id           NUMBER REFERENCES ADVOCATE1(a_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE adv_photo_seq START WITH 1 INCREMENT BY 1;

-- JUNIOR ADVOCATE PHOTO TABLE
CREATE TABLE JUNIOR_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    ja_id          NUMBER REFERENCES JUNIOR_ADVOCATE1(ja_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE junior_photo_seq START WITH 1 INCREMENT BY 1;

-- STAFF PHOTO TABLE
CREATE TABLE STAFF_PHOTO (
    photo_id       NUMBER PRIMARY KEY,
    st_id          NUMBER REFERENCES STAFF1(st_id),
    photo_path     VARCHAR2(500),
    photo_data     BLOB,
    uploaded_date  DATE DEFAULT SYSDATE,
    is_active      NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE staff_photo_seq START WITH 1 INCREMENT BY 1;

-- SUPPORT STAFF ROLE TABLE
CREATE TABLE SUPPORT_STAFF_ROLE (
    role_id        NUMBER PRIMARY KEY,
    st_id          NUMBER REFERENCES STAFF1(st_id),
    department     VARCHAR2(50),
    responsibilities VARCHAR2(500),
    assigned_date  DATE DEFAULT SYSDATE
);

CREATE SEQUENCE support_role_seq START WITH 1 INCREMENT BY 1;

-- SYNONYMS FOR LEGACY OBJECT NAMES
CREATE SYNONYM CASE_EVENT FOR CASE_EVENT1;
CREATE SYNONYM EVIDENCE FOR EVIDENCE1;
CREATE SYNONYM COMMUNICATION FOR COMMUNICATION1;
-- ============================================================
-- SEED DATA
-- ============================================================

-- Insert default admin user
INSERT INTO AMS_USERS1 VALUES (user_seq.NEXTVAL, 'admin', 'admin123', 'STAFF', 1, 1, SYSDATE);
INSERT INTO AMS_USERS1 VALUES (user_seq.NEXTVAL, 'advocate1', 'adv123', 'SENIOR_ADVOCATE', 1, 1, SYSDATE);
INSERT INTO AMS_USERS1 VALUES (user_seq.NEXTVAL, 'junior1', 'jun123', 'JUNIOR_ADVOCATE', 1, 1, SYSDATE);
INSERT INTO AMS_USERS1 VALUES (user_seq.NEXTVAL, 'client1', 'cli123', 'CLIENT', 1, 1, SYSDATE);

-- Insert sample advocate
INSERT INTO ADVOCATE1 VALUES (adv_seq.NEXTVAL,'Rajesh Kumar','BAR/2010/1234','rajesh@ams.com','9876543210','ACTIVE','R-101',TO_DATE('2010-06-15','YYYY-MM-DD'),'Chennai','Anna Nagar',600040,'Criminal Law',14,1975,NULL,4.5,NULL);
INSERT INTO JUNIOR_ADVOCATE1 VALUES (ja_seq.NEXTVAL,'Priya Devi','priya@ams.com','9876500001','LLB',TO_DATE('2022-01-10','YYYY-MM-DD'),'Criminal',1,'ACTIVE','D-5',0,NULL);
INSERT INTO STAFF1 VALUES (staff_seq.NEXTVAL,'Suresh Admin','9000000001','Admin Manager','Chennai',600001,'Tamil Nadu',8,'9000000002',45000,TO_DATE('1985-05-20','YYYY-MM-DD'),TO_DATE('2018-03-01','YYYY-MM-DD'),'ADMIN');
INSERT INTO CLIENT1 VALUES (client_seq.NEXTVAL,'Arun Patel','arun@gmail.com','9111111111','TN123456','INDIVIDUAL','Chennai','T Nagar',600017,SYSDATE,NULL,'client1','cli123',NULL,0);

COMMIT;

-- ============================================================
-- INDEXES, VIEWS, FUNCTIONS, PROCEDURES, TRIGGERS
-- ============================================================

CREATE INDEX IDX_CASES1_CID ON CASES1(c_id);
CREATE INDEX IDX_CASES1_ASSIGNED_ADV ON CASES1(assigned_adv);
CREATE INDEX IDX_ADVOCATE_CASE1_AID ON ADVOCATE_CASE1(a_id);
CREATE INDEX IDX_ADVOCATE_CASE1_CASEID ON ADVOCATE_CASE1(case_id);
CREATE INDEX IDX_EVIDENCE1_CASE_ID ON EVIDENCE1(case_id);
CREATE INDEX IDX_COMMUNICATION1_CASE_ID ON COMMUNICATION1(case_id);
CREATE INDEX IDX_LOG_COMM_AID ON LOG_COMMUNICATION(a_id);
CREATE INDEX IDX_REMINDER1_AID ON REMINDER1(a_id);
CREATE INDEX IDX_TASK_ASSIGNMENT1_ASSIGNED_TO ON TASK_ASSIGNMENT1(assigned_to);
CREATE INDEX IDX_PAYMENT1_CASE_ID ON PAYMENT1(case_id);
CREATE INDEX IDX_PREFERRED_ADVOCATE1_AID ON PREFERRED_ADVOCATE1(a_id);
CREATE INDEX IDX_REPRESENTS1_CID ON REPRESENTS1(c_id);

-- Trigger to update staff fee totals when payments are marked as paid
CREATE OR REPLACE TRIGGER TRG_PAYMENT1_STAFF_UPDATE
AFTER INSERT OR UPDATE OF payment_status, amount, pay_mode, handled_by ON PAYMENT1
FOR EACH ROW
BEGIN
    IF INSERTING AND :NEW.payment_status = 'PAID' AND :NEW.handled_by IS NOT NULL THEN
        UPDATE STAFF1
        SET total_fees_collected = NVL(total_fees_collected, 0) + :NEW.amount,
            last_payment_date = :NEW.pay_date,
            last_payment_mode = :NEW.pay_mode
        WHERE st_id = :NEW.handled_by;
    ELSIF UPDATING AND :NEW.payment_status = 'PAID' AND :OLD.payment_status != 'PAID' AND :NEW.handled_by IS NOT NULL THEN
        UPDATE STAFF1
        SET total_fees_collected = NVL(total_fees_collected, 0) + :NEW.amount,
            last_payment_date = :NEW.pay_date,
            last_payment_mode = :NEW.pay_mode
        WHERE st_id = :NEW.handled_by;
    END IF;
END TRG_PAYMENT1_STAFF_UPDATE;
/

-- Views for reporting and dashboard summaries
CREATE OR REPLACE VIEW VW_CASE_SUMMARY AS
SELECT c.case_id,
       c.c_title,
       c.status,
       c.priority_level,
       c.filed_date,
       c.court_name,
       c.fee_amount,
       c.total_fees,
       cli.c_name AS client_name,
       a.a_name AS advocate_name
FROM CASES1 c
LEFT JOIN CLIENT1 cli ON c.c_id = cli.c_id
LEFT JOIN ADVOCATE1 a ON c.assigned_adv = a.a_id;

CREATE OR REPLACE VIEW VW_ADVOCATE_WORKLOAD AS
SELECT a.a_id,
       a.a_name,
       COUNT(DISTINCT ac.case_id) AS total_cases,
       NVL(SUM(p.amount),0) AS total_payments,
       AVG(a.rating) AS average_rating
FROM ADVOCATE1 a
LEFT JOIN ADVOCATE_CASE1 ac ON a.a_id = ac.a_id
LEFT JOIN PAYMENT1 p ON ac.case_id = p.case_id
GROUP BY a.a_id, a.a_name;

CREATE OR REPLACE VIEW VW_CLIENT_CASE_HISTORY AS
SELECT c.c_id,
       c.c_name,
       cs.case_id,
       cs.c_title,
       cs.status,
       cs.filed_date,
       cs.court_name,
       cs.fee_amount,
       cs.assigned_adv
FROM CLIENT1 c
LEFT JOIN CASES1 cs ON c.c_id = cs.c_id;

-- Function to get advocate case count
CREATE OR REPLACE FUNCTION GET_ADVOCATE_CASE_COUNT(p_advocate_id NUMBER)
RETURN NUMBER IS
    v_count NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM ADVOCATE_CASE1
    WHERE a_id = p_advocate_id;
    RETURN NVL(v_count,0);
END GET_ADVOCATE_CASE_COUNT;
/

-- Function to compute balance for a case
CREATE OR REPLACE FUNCTION GET_CASE_BALANCE(p_case_id NUMBER)
RETURN NUMBER IS
    v_total_paid NUMBER := 0;
    v_fee_amount NUMBER := 0;
    v_total_fees NUMBER := 0;
BEGIN
    SELECT NVL(fee_amount,0), NVL(total_fees,0)
    INTO v_fee_amount, v_total_fees
    FROM CASES1
    WHERE case_id = p_case_id;

    SELECT NVL(SUM(amount),0)
    INTO v_total_paid
    FROM PAYMENT1
    WHERE case_id = p_case_id;

    RETURN (v_fee_amount + v_total_fees) - v_total_paid;
END GET_CASE_BALANCE;
/

-- Procedure to add a new case and preferred advocates
CREATE OR REPLACE PROCEDURE PR_ADD_NEW_CASE(
    p_client_id      NUMBER,
    p_title          VARCHAR2,
    p_case_type      VARCHAR2,
    p_law_category   VARCHAR2,
    p_problem_desc   CLOB,
    p_priority       NUMBER,
    p_court_name     VARCHAR2,
    p_fee_amount     NUMBER,
    p_ca_name        VARCHAR2,
    p_assigned_adv   NUMBER,
    p_preferred_adv1 NUMBER DEFAULT NULL,
    p_preferred_adv2 NUMBER DEFAULT NULL,
    p_preferred_adv3 NUMBER DEFAULT NULL
) IS
    v_case_id NUMBER;
BEGIN
    INSERT INTO CASES1(case_id,c_id,c_title,c_type,law_category,problem_desc,status,priority_level,court_name,fee_amount,total_fees,ca_name,assigned_adv)
    VALUES(case_seq.NEXTVAL, p_client_id, p_title, p_case_type, p_law_category, p_problem_desc, 'PENDING', p_priority, p_court_name, NVL(p_fee_amount,0), 0, p_ca_name, p_assigned_adv);

    SELECT case_seq.CURRVAL INTO v_case_id FROM dual;

    INSERT INTO ADVOCATE_CASE1(a_id,case_id)
    VALUES(p_assigned_adv, v_case_id);

    IF p_preferred_adv1 IS NOT NULL THEN
        INSERT INTO PREFERRED_ADVOCATE1(pref_id,case_id,a_id,pref_rank,status)
        VALUES(pref_seq.NEXTVAL, v_case_id, p_preferred_adv1, 1, 'PENDING');
    END IF;
    IF p_preferred_adv2 IS NOT NULL THEN
        INSERT INTO PREFERRED_ADVOCATE1(pref_id,case_id,a_id,pref_rank,status)
        VALUES(pref_seq.NEXTVAL, v_case_id, p_preferred_adv2, 2, 'PENDING');
    END IF;
    IF p_preferred_adv3 IS NOT NULL THEN
        INSERT INTO PREFERRED_ADVOCATE1(pref_id,case_id,a_id,pref_rank,status)
        VALUES(pref_seq.NEXTVAL, v_case_id, p_preferred_adv3, 3, 'PENDING');
    END IF;

    COMMIT;
END PR_ADD_NEW_CASE;
/

-- Procedure to log communication with follow-up
CREATE OR REPLACE PROCEDURE PR_LOG_COMMUNICATION_ENTRY(
    p_case_id       NUMBER,
    p_client_id     NUMBER,
    p_advocate_id   NUMBER,
    p_log_type      VARCHAR2,
    p_subject       VARCHAR2,
    p_details       CLOB,
    p_notes         VARCHAR2,
    p_follow_up_date DATE,
    p_follow_up_status VARCHAR2 DEFAULT 'PENDING'
) IS
BEGIN
    INSERT INTO LOG_COMMUNICATION(log_comm_id, case_id, c_id, a_id, log_type, log_subject, log_details, log_notes, log_date, log_time, follow_up_date, follow_up_status)
    VALUES(log_comm_seq.NEXTVAL, p_case_id, p_client_id, p_advocate_id, p_log_type, p_subject, p_details, p_notes, NVL(p_follow_up_date,SYSDATE), SYSTIMESTAMP, p_follow_up_date, NVL(p_follow_up_status,'PENDING'));
    COMMIT;
END PR_LOG_COMMUNICATION_ENTRY;
/

-- Procedure to add a timeline event
CREATE OR REPLACE PROCEDURE PR_ADD_TIMELINE_EVENT(
    p_case_id    NUMBER,
    p_title      VARCHAR2,
    p_description CLOB,
    p_status     VARCHAR2 DEFAULT 'ON_TRACK'
) IS
BEGIN
    INSERT INTO TIMELINE1(time_id, case_id, entry_date, title, e_data, status_indicator)
    VALUES(timeline_seq.NEXTVAL, p_case_id, SYSDATE, p_title, p_description, p_status);
    COMMIT;
END PR_ADD_TIMELINE_EVENT;
/

-- Trigger to maintain total fees when payments change
CREATE OR REPLACE TRIGGER TRG_PAYMENT1_RECALC_TOTAL_FEES
FOR INSERT OR UPDATE OR DELETE ON PAYMENT1
COMPOUND TRIGGER
    TYPE t_case_id_tab IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    v_case_ids t_case_id_tab;
    v_index PLS_INTEGER := 0;

    PROCEDURE add_case(p_case_id NUMBER) IS
    BEGIN
        IF p_case_id IS NOT NULL THEN
            v_index := v_index + 1;
            v_case_ids(v_index) := p_case_id;
        END IF;
    END add_case;

    BEFORE STATEMENT IS
    BEGIN
        v_index := 0;
    END BEFORE STATEMENT;

    AFTER EACH ROW IS
    BEGIN
        IF INSERTING OR UPDATING THEN
            add_case(:NEW.case_id);
        END IF;
        IF DELETING THEN
            add_case(:OLD.case_id);
        END IF;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        FOR i IN 1..v_index LOOP
            UPDATE CASES1
            SET total_fees = NVL((SELECT SUM(amount) FROM PAYMENT1 WHERE case_id = v_case_ids(i)), 0)
            WHERE case_id = v_case_ids(i);
        END LOOP;
    END AFTER STATEMENT;
END TRG_PAYMENT1_RECALC_TOTAL_FEES;
/

-- Trigger to ensure default task assignment fields are populated
CREATE OR REPLACE TRIGGER TRG_TASK_ASSIGNMENT1_BEFORE
BEFORE INSERT ON TASK_ASSIGNMENT1
FOR EACH ROW
BEGIN
    IF :NEW.status IS NULL THEN
        :NEW.status := 'PENDING';
    END IF;
    IF :NEW.assigned_dt IS NULL THEN
        :NEW.assigned_dt := SYSDATE;
    END IF;
END TRG_TASK_ASSIGNMENT1_BEFORE;
/

-- Trigger to set default values for communication log entries
CREATE OR REPLACE TRIGGER TRG_LOG_COMMUNICATION_DEFAULTS
BEFORE INSERT ON LOG_COMMUNICATION
FOR EACH ROW
BEGIN
    IF :NEW.log_date IS NULL THEN
        :NEW.log_date := SYSDATE;
    END IF;
    IF :NEW.log_time IS NULL THEN
        :NEW.log_time := SYSTIMESTAMP;
    END IF;
    IF :NEW.follow_up_status IS NULL THEN
        :NEW.follow_up_status := 'PENDING';
    END IF;
END TRG_LOG_COMMUNICATION_DEFAULTS;
/

-- Trigger to validate evidence file path
CREATE OR REPLACE TRIGGER TRG_EVIDENCE1_BEFORE
BEFORE INSERT OR UPDATE ON EVIDENCE1
FOR EACH ROW
BEGIN
    IF :NEW.file_path IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Evidence file_path is required.');
    END IF;
END TRG_EVIDENCE1_BEFORE;
/

-- Sample anonymous PL/SQL block for validation
DECLARE
    l_adv_case_count NUMBER;
    l_case_balance   NUMBER;
BEGIN
    l_adv_case_count := GET_ADVOCATE_CASE_COUNT(1);
    DBMS_OUTPUT.PUT_LINE('Advocate #1 case count: ' || l_adv_case_count);

    l_case_balance := GET_CASE_BALANCE(1);
    DBMS_OUTPUT.PUT_LINE('Case #1 balance: ' || l_case_balance);
END;
/
