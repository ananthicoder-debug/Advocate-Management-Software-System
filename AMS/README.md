# Advocate Management System (AMS)
## Complete Legal Case Management Software — Java + Oracle SQL*Plus

---

## Project Overview
- **Name**: Advocate Management System (AMS)
- **Technology**: Java (Swing UI) + Oracle SQL*Plus (JDBC)
- **Architecture**: Standalone Desktop Application
- **Java Version**: Java 11+
- **Target Platform**: Windows / Linux / macOS (any JRE 11+ system)

---

## Role-Based Access Control
The system supports **4 roles** with dedicated dashboards:

| Role | Username | Password | Capabilities |
|------|----------|----------|--------------|
| **Senior Advocate** | advocate1 | adv123 | Full case management, client mgmt, junior assignment |
| **Junior Advocate** | junior1 | jun123 | View assigned tasks & cases, client contact |
| **Client** | client1 | cli123 | File cases, track status, communicate |
| **Staff / Admin** | admin | admin123 | Full system admin, reports, salary, assignment |

---

## Features Implemented

### Welcome / Splash Screen
- Animated splash with scales-of-justice icon
- Fade-in animation + rotating ring
- Bright gradient background (Navy → Royal Blue)
- Click-to-proceed after loading

### Login Screen
- Role selector (Senior Advocate / Junior Advocate / Client / Staff)
- Split-panel design — decorative left, login card right
- Client self-registration link
- Database authentication with demo-mode fallback

### Senior Advocate Dashboard
- **Dashboard Home**: 5 summary cards (Cases, Clients, Hearings, Tasks, New Requests)
- **Case Management**: Full CRUD, search/filter, journey timeline
  - **Case Journey**: Year-based pathway timeline, events per year
  - **Hearings Tab**: Add/view cumulative hearings
  - **Evidence Tab**: Add/view evidence, grant junior access
  - **Notes Tab**: Private legal notes with follow-up tracking
  - **Client Chat Tab**: Message client per case
- **New Case Requests**: Accept/reject with auto-timeline alignment
- **Hearings**: Aggregate hearing schedule across all cases
- **Evidence**: Cross-case evidence management
- **Strategy & Notes**: Legal arguments with law sections
- **Timeline**: Case milestone tracker with status indicators
- **Clients Page**: Client list with case drill-down
- **Junior Management**: Assign tasks, view completion, rate work
- **Communications**: Log all client interactions
- **Reminders**: Priority-based reminder system with done tracking
- **Profile**: View & update advocate profile

### Junior Advocate Dashboard
- **Dashboard Home**: Summary cards (Tasks, Cases, Clients)
- **My Tasks**: All assigned tasks with Mark Done functionality
- **Assigned Cases**: Cases shared by senior advocate
- **My Clients**: Clients accessible via assigned cases

### Client Dashboard
- **Dashboard Home**: 4 cards (Active, Pending, Hearings, Messages)
- **File a Case**: Full form with preferred advocate selection (3 preferences)
- **Case Status**: Progress bar for each case with status colour coding
- **Case Timeline**: Milestone view
- **Hearing Schedule**: Client-facing hearing list
- **Messages**: Chat interface with advocate
- **Documents**: Document management

### Staff / Admin Dashboard
- **System Overview**: 5 admin summary cards
- **Advocates**: Full advocate management table
- **Junior Advocates**: Junior management table
- **Clients**: Client records
- **Support Staff**: Support staff management
- **All Cases**: Full case registry with all details
- **Hearings**: Global hearing schedule
- **Case Assignment**: Assign advocate to any case
- **Salary & Fees**: Salary management with ratings-based recommendations
- **Payments**: Payment records and bill management
- **Reports**: 8 types — Advocate Performance, Client Analytics, Case Statistics, Financial Report, Staff Report, Timeline Analysis, Hearing Summary, Evidence Report

---

## Database Schema (Oracle SQL)
**Tables created in `sql/schema.sql`:**
- `AMS_USERS` — Login credentials
- `ADVOCATE` — Senior advocate profiles
- `JUNIOR_ADVOCATE` — Junior advocate profiles
- `STAFF` — Administrative & support staff
- `CLIENT` — Client records
- `CASES` — Case master
- `ADVOCATE_CASE` — Advocate-case mapping
- `JUNIOR_CASE` — Junior advocate case access
- `HEARING` — Court hearing schedule
- `EVIDENCE` — Evidence repository
- `EVIDENCE_ACCESS` — Evidence access audit log
- `STRATEGY` — Legal strategy & arguments
- `NOTE` — Advocate notes per case
- `COMMUNICATION` — Client communication log
- `CASE_EVENT` — Case event history
- `TIMELINE` — Case milestone timeline
- `REMINDER` — Reminders & alerts
- `TASK_ASSIGNMENT` — Junior task assignments
- `PAYMENT` — Payment records
- `PREFERRED_ADVOCATE` — Client's advocate preferences
- `REPRESENTS` — Advocate-client-case representation

---

## Project Structure
```
AMS/
├── src/
│   └── com/ams/
│       ├── Main.java                    # Entry point
│       ├── splash/
│       │   └── SplashScreen.java        # Welcome screen
│       ├── login/
│       │   ├── LoginFrame.java          # Login with role selection
│       │   └── ClientRegistrationDialog.java
│       ├── util/
│       │   ├── AMSTheme.java            # Color palette & fonts
│       │   └── DBConnection.java        # Oracle JDBC utility
│       ├── components/
│       │   ├── RoundedButton.java       # Custom button
│       │   ├── DashboardCard.java       # Gradient stat card
│       │   └── LabeledField.java        # Input field with label
│       ├── dashboard/
│       │   ├── BaseDashboard.java       # Base frame (sidebar + topbar)
│       │   ├── AdvocateDashboard.java   # Senior advocate
│       │   ├── JuniorDashboard.java     # Junior advocate
│       │   ├── ClientDashboard.java     # Client portal
│       │   └── StaffDashboard.java      # Admin staff
│       └── advocate/
│           ├── CasesPage.java
│           ├── CaseJourneyDialog.java   # Full case journey UI
│           ├── HearingsPage.java
│           ├── EvidencePage.java
│           ├── StrategyPage.java
│           ├── TimelinePage.java
│           ├── ClientsPage.java
│           ├── JuniorManagePage.java
│           ├── CommunicationsPage.java
│           ├── RemindersPage.java
│           └── NewCasePage.java
├── sql/
│   └── schema.sql                       # Complete Oracle DDL + seed data
├── lib/
│   └── (place ojdbc11.jar here)         # Oracle JDBC driver
├── AMS.jar                              # Compiled executable JAR
├── build.sh                             # Linux/Mac build script
├── build.bat                            # Windows build script
└── run.bat                              # Windows quick-run
```

---

## Setup & Run Instructions

### Prerequisites
1. **Java 11 or higher** (JRE for running, JDK for compiling)
2. **Oracle Database XE** (or any Oracle edition with SQL*Plus)
3. **Oracle JDBC Driver** (`ojdbc11.jar` from Oracle website)

### Database Setup
```sql
-- Run in SQL*Plus as system/oracle:
@sql/schema.sql
```

### DB Connection Configuration
Edit `src/com/ams/util/DBConnection.java`:
```java
private static final String DB_URL  = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String DB_USER = "system";
private static final String DB_PASS = "oracle";
```

### Running the Application
```bash
# Option 1: Run pre-built JAR (with Oracle JDBC in classpath)
java -cp AMS.jar:lib/ojdbc11.jar com.ams.Main

# Option 2: Run without DB (demo mode — works immediately!)
java -jar AMS.jar
```

> **Demo Mode**: The application works fully without a database connection using built-in sample data. All UI screens, navigation, and interactions are fully functional.

### Build from Source
```bash
# Linux/Mac
chmod +x build.sh && ./build.sh

# Windows
build.bat
```

---

## UI Design Highlights
- **Color Palette**: Deep Navy (#0A2540) → Royal Blue (#1A4B8C) → Gold (#F0A500)
- **Gradient cards** with icon, count value, click navigation
- **Animated splash screen** with rotating ring and fade-in
- **Sidebar navigation** with active state highlight and hover effects
- **Chat bubble UI** for client-advocate communication
- **Journey timeline** with year-node pathway for case history
- **Progress bars** for case completion status
- **Row-striped tables** with color-coded status cells

---

## Deployment Status
- **Platform**: Standalone Java Desktop
- **Status**: ✅ Compiled & Ready
- **Last Updated**: March 2025
- **Tech Stack**: Java 11 Swing + Oracle SQL*Plus (JDBC Thin)
