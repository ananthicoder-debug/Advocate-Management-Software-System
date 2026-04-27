# Client Case Filing + Evidence DB Integration Fix

## Issues Found
- Demo data fallbacks when DB null (hardcoded displays)
- Evidence upload stores files + DB records (good), but demo everywhere
- All displays must query DB only

## Plan
**Information Gathered:**
- ClientDashboard.java: File case evidence upload → EVIDENCE1 INSERT + file copy (good)
- All loads (cases, hearings, timeline, messages, documents) have DB query + demo fallback
- EVIDENCE1 perfect for file_path storage/display

**Detailed Update Plan:**
1. ClientDashboard.java:
   - Remove all addDemo*() calls - show empty/"No data" if no DB results
   - submitCaseWithEvidence(): Make DB required - error if null
   - buildCaseStatusPage(): loadCaseStatus() - no demo, "No cases" panel if empty
   - buildHomePage(): loadMyCases() - empty if no DB
   - buildHearingsPage(): empty table if no DB
   - buildTimelinePage(): "No timeline" if empty
   - buildMessagesPage(): "No messages" if empty
   - buildDocumentsPage(): Good (queries EVIDENCE1)

**Dependent Files:** None

**Followup:** 
- Edit ClientDashboard.java 
- Test run.bat → login client → file case + evidence → verify DB EVIDENCE1 populated, no demo data shown
- Update TODO complete

<ask_followup_question>Approve this plan to remove all hardcoded demo data from ClientDashboard.java, ensure all case/evidence displays are DB-driven only? Ready to edit?</ask_followup_question>
