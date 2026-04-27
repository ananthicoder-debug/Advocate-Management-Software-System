@echo off
REM Quick run script for AMS
SET LIB_DIR=lib
SET OUT_DIR=out

if defined JAVA_HOME (
    SET JAVA_CMD=%JAVA_HOME%\bin\java
) else (
    SET JAVA_CMD=java
)

echo Using Java command: %JAVA_CMD%
if exist "%OUT_DIR%" (
    echo Running from compiled classes in %OUT_DIR%...
    "%JAVA_CMD%" -cp "%OUT_DIR%;%LIB_DIR%\ojdbc8.jar" com.ams.Main
) else (
    echo Running from AMS.jar...
    "%JAVA_CMD%" -jar AMS.jar
)
