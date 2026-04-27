@echo off
REM ============================================================
REM AMS — Build Script for Windows
REM ============================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET SRC_DIR=src
SET OUT_DIR=out
SET LIB_DIR=lib
SET JAR_NAME=AMS.jar
SET MAIN_CLASS=com.ams.Main

if defined JAVA_HOME (
    SET JAR_CMD=%JAVA_HOME%\bin\jar
) else (
    SET JAR_CMD=jar
)

echo === Advocate Management System — Build ===

echo Creating output directory...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

echo Compiling Java sources...
dir /s /b %SRC_DIR%\*.java > sources.txt
if exist "%LIB_DIR%\ojdbc8.jar" (
    echo javac --release 11 -d %OUT_DIR% -cp "%LIB_DIR%\ojdbc8.jar" -sourcepath %SRC_DIR% @sources.txt
    javac --release 11 -d %OUT_DIR% -cp "%LIB_DIR%\ojdbc8.jar" -sourcepath %SRC_DIR% @sources.txt
) else (
    echo javac --release 11 -d %OUT_DIR% -sourcepath %SRC_DIR% @sources.txt
    javac --release 11 -d %OUT_DIR% -sourcepath %SRC_DIR% @sources.txt
)
if errorlevel 1 (
    echo.
    echo Compilation failed. See errors above.
    del sources.txt
    if exist build_success.txt del build_success.txt
    exit /b 1
)

echo Build succeeded > build_success.txt

echo Writing manifest...
echo Main-Class: %MAIN_CLASS% > %OUT_DIR%\manifest.mf
echo Class-Path: lib/ojdbc8.jar >> %OUT_DIR%\manifest.mf

echo Creating JAR...
cd %OUT_DIR%
"%JAR_CMD%" cfm ..\%JAR_NAME% manifest.mf .
if errorlevel 1 (
    echo.
    echo JAR creation failed. Running from classes in %OUT_DIR% instead.
    cd ..
    del sources.txt
    exit /b 0
)
cd ..

del sources.txt

echo.
echo Build successful!
echo JAR: %JAR_NAME%
if exist "%OUT_DIR%" echo Classes available in %OUT_DIR%
echo.
echo To run:
echo   .\run.bat
echo.
echo Note: Oracle JDBC driver should be in lib\ojdbc8.jar
