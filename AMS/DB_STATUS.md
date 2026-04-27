# AMS Database Setup Guide

## 1. Install Oracle XE (if not installed)
- Download Oracle Database 21c Express Edition for Windows
- Install with defaults (port 1521, SID XE)
- Set password for SYSTEM user (default SYSTEM)

## 2. Start Oracle Service
- Windows Services (services.msc)
- Start "OracleServiceXE" and "OracleXETNSListener"

## 3. Test Connection
```
cd c:/Users/Admin/Documents/home/AMS
javac -cp ".;lib/ojdbc8.jar" TestDatabaseConnection.java
java -cp ".;lib/ojdbc8.jar" TestDatabaseConnection
```
Expected: Table counts, sample records.

## 4. Run Schema
```
sqlplus system/SYSTEM@XE
@sql/schema.sql
@sql/verify_data.sql
```

## 5. Test App
```
compile.ps1
run.bat
```
- Login, add case, logout/login → case persists.
- DB status in topbar "Connected"

## Troubleshooting
- Connection "Disconnected: Primary connection failed: IO Error: The Network Adapter could not establish the connection"
  - Oracle service not running
- "ORA-01017: invalid username/password"
  - SYSTEM password wrong
- No ojdbc8.jar: Download oracle jdbc 8

Changes persist across restarts only with DB connected!

