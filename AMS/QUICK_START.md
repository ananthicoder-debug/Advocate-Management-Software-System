# AMS - Quick Start Guide

## What Was Fixed
✅ **Login screen now enters the next screen properly**
✅ **Demo mode works automatically when database is unavailable**  
✅ **Clear error messages distinguish between connection errors and invalid credentials**

---

## How to Run

### Option 1: Demo Mode (No Database Setup Required) ⭐ RECOMMENDED
```bash
java -jar AMS.jar
```
- Enter ANY username and password
- Select any role (Senior Advocate, Junior Advocate, Client, Staff)
- Click LOGIN
- You'll see: "ℹ Using demo mode (database unavailable)"
- Full interface works with sample data

### Option 2: With Oracle Database
```bash
# First, setup Oracle database with schema.sql
java -cp AMS.jar:lib/ojdbc11.jar com.ams.Main
```

**Pre-configured test accounts:**
| Role | Username | Password |
|------|----------|----------|
| Senior Advocate | advocate1 | adv123 |
| Junior Advocate | junior1 | jun123 |
| Client | client1 | cli123 |
| Staff | admin | admin123 |

---

## What Changed in the Code

**File:** `src/com/ams/login/LoginFrame.java`

**Method:** `doLogin()` (lines 302-354)

**Improvement:** 
- Now distinguishes between database connection errors and invalid credentials
- Automatically enables demo mode when database is unavailable
- Shows appropriate status messages for each scenario

---

## Test the Fix

### Test 1: Demo Mode Login
1. Run: `java -jar AMS.jar`
2. Enter: username=`test`, password=`test`
3. Select: Any role
4. **Result:** Should proceed to dashboard with info message

### Test 2: Database with Valid Credentials  
1. Setup Oracle DB with `sql/schema.sql`
2. Run: `java -cp AMS.jar:lib/ojdbc11.jar com.ams.Main`
3. Enter: username=`advocate1`, password=`adv123`
4. Select: Senior Advocate
5. **Result:** Should proceed to Senior Advocate dashboard

### Test 3: Database with Invalid Credentials
1. Database running
2. Run: `java -cp AMS.jar:lib/ojdbc11.jar com.ams.Main`
3. Enter: username=`admin`, password=`wrongpass`
4. **Result:** Should show error message and stay on login screen

---

## Rebuild from Source (If Needed)

### Windows
```bash
build.bat
```

### Linux/Mac
```bash
chmod +x build.sh
./build.sh
```

---

## Features Now Working

✅ Full-screen login frame  
✅ Role-based access control  
✅ Demo mode fallback  
✅ Clear error messages  
✅ Navigation to dashboard after login  
✅ All 4 dashboard types (Senior Advocate, Junior, Client, Staff)  

---

## Support

If you encounter issues:
1. Check that Java 11+ is installed: `java -version`
2. For database mode, verify Oracle connection in `src/com/ams/util/DBConnection.java`
3. Run in demo mode first to verify UI works
