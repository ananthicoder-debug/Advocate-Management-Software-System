 th# Profile Header Refresh After Update

**Approved Plan:**
- BaseDashboard.java: Add refreshHeader() - reload name/photo from DB (role/ref_id)
- All dashboard update dialogs: After DB UPDATE → parent.refreshHeader() + local refresh

**Steps:**
- [ ] Step 1: Read BaseDashboard.java
- [ ] Step 2: Add refreshHeader() method
- [ ] Step 3: Update AdvocateDashboard/Cli ent/Staff/Junior dialogs call refreshHeader()
- [ ] Step 4: Test all roles profile update → header name/photo updates

**Status: Starting**
t 