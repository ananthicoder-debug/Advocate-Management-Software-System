@echo off
REM Simple AMS Build Script

SET SRC_DIR=src
SET OUT_DIR=out
SET JAR_NAME=AMS.jar
SET MAIN_CLASS=com.ams.Main

echo === Building AMS ===

REM Create output directory
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Generate sources list
dir /s /b "%SRC_DIR%\*.java" > sources_build.txt

REM Compile
echo Compiling...
javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" @sources_build.txt

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    
    REM Create manifest
    (
        echo Manifest-Version: 1.0
        echo Main-Class: %MAIN_CLASS%
        echo Class-Path: lib/ojdbc11.jar
    ) > "%OUT_DIR%\manifest.mf"
    
    REM Create JAR
    cd "%OUT_DIR%"
    jar cfm "..\%JAR_NAME%" manifest.mf .
    cd ..
    
    del sources_build.txt
    echo Build complete: %JAR_NAME%
) else (
    echo Compilation failed!
    pause
)
