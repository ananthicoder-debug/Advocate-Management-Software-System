# Complete AMS Implementation Plan - All Features

## Requirements Summary

### 1. Staff Login Fix
**Issue:** Staff account showing "invalid credentials"
**Solution:** Verify role code mapping and ensure STAFF role validation works
**File:** `src/com/ams/login/LoginFrame.java`

### 2. Full Screen Mode for All Screens
**Requirement:** All screens must be displayed in full screen
**Solution:** 
- Update all Dashboard classes to use maximized state
- Remove fixed sizes, use screen dimensions
- Update all dialog/frame sizes to fullscreen
**Files:** BaseDashboard.java, All dashboard classes, all page classes

### 3. Photo Upload During Client Registration
**Requirement:** Client registration must include photo upload
**Solution:**
- Add photo selector/upload button in ClientRegistrationDialog
- Store photo as BLOB in database
- Create file chooser dialog
**Files:** ClientRegistrationDialog.java, Database schema

### 4. Photo Display/Upload in Profiles
**Requirement:** Photos in advocate, junior advocate, and staff profiles
**Solution:**
- Add photo fields to all profile pages
- Add upload functionality
- Display photo in profile with edit capability
- Update profile on change
**Files:** AdvocateDashboard.java, JuniorDashboard.java, StaffDashboard.java (profile sections)

### 5. Document Upload for Case Filing
**Requirement:** Upload any type of document when filing cases
**Solution:**
- Add document upload button in file case dialog
- Browse local file system
- Support all file types
- Store document path/binary in database
**Files:** ClientDashboard.java (FileCaseDialog), Database updates

### 6. Evidence File Viewer
**Requirement:** Click evidence to auto-open and view any file type
**Solution:**
- Add click listener to evidence files
- Detect file type
- Open with default system application
- Handle all document types (.pdf, .doc, .docx, .txt, .jpg, .png, etc.)
**Files:** EvidencePage.java, CaseJourneyDialog.java

---

## Implementation Steps (In Order)

1. Create utility classes for file handling
2. Update database schema for photo/document storage
3. Fix staff login issue
4. Update all screens to fullscreen
5. Add photo management components
6. Add document upload/viewer functionality
7. Update all affected screens and dialogs
8. Test all functionality

---

## Files to Create/Modify

### NEW FILES TO CREATE:
- `FileUploadUtil.java` - File handling utilities
- `PhotoUtils.java` - Photo processing utilities
- `DocumentViewerUtil.java` - File viewer integration

### FILES TO MODIFY:
1. `src/com/ams/login/LoginFrame.java` - Staff login + fullscreen
2. `src/com/ams/login/ClientRegistrationDialog.java` - Photo upload
3. `src/com/ams/dashboard/BaseDashboard.java` - Fullscreen setup
4. `src/com/ams/dashboard/AdvocateDashboard.java` - Photo profile + fullscreen
5. `src/com/ams/dashboard/JuniorDashboard.java` - Photo profile + fullscreen
6. `src/com/ams/dashboard/ClientDashboard.java` - Photo profile + document upload + fullscreen
7. `src/com/ams/dashboard/StaffDashboard.java` - Photo profile + fullscreen
8. `src/com/ams/advocate/EvidencePage.java` - File viewer
9. `src/com/ams/advocate/CaseJourneyDialog.java` - Photo display + file viewer
10. `src/com/ams/splash/SplashScreen.java` - Fullscreen optimization
11. `sql/schema.sql` - Add photo/document columns

---

## Expected Timeline
- File handling utilities: 2 files
- Full screen updates: 6 files
- Photo management: 4 files
- Document handling: 3 files
- Total: 15 files to create/modify

---

## Success Criteria
✅ Staff can login with admin/admin123
✅ All screens occupy full monitor space
✅ Client registration accepts photo upload
✅ All user profiles show and accept photos
✅ Case filing accepts document uploads
✅ Evidence files open when clicked
✅ All changes persist in database
✅ Photo updates reflected in real-time
