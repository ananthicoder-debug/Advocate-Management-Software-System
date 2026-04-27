#!/bin/bash
# ============================================================
# AMS — Build Script
# ============================================================
set -e

SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"
JAR_NAME="AMS.jar"
MAIN_CLASS="com.ams.Main"

# Auto-detect javac
JAVAC=$(which javac 2>/dev/null || find /opt/java -name "javac" 2>/dev/null | head -1)
JAR_CMD=$(which jar 2>/dev/null || find /opt/java -name "jar" 2>/dev/null | head -1)

echo "=== Advocate Management System — Build ==="
echo "    Using javac: $JAVAC"

mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > sources.txt

echo "Compiling Java sources..."
if [ -f "$LIB_DIR/ojdbc8.jar" ]; then
    "$JAVAC" -encoding UTF-8 -source 11 -target 11 -d "$OUT_DIR" -cp "$LIB_DIR/ojdbc8.jar" -sourcepath "$SRC_DIR" @sources.txt
else
    "$JAVAC" -encoding UTF-8 -source 11 -target 11 -d "$OUT_DIR" -sourcepath "$SRC_DIR" @sources.txt
fi

echo "Creating JAR..."
cd "$OUT_DIR"
cat > manifest.mf << MANIFEST
Main-Class: $MAIN_CLASS
Class-Path: lib/ojdbc11.jar
MANIFEST

"$JAR_CMD" cfm "../$JAR_NAME" manifest.mf .
cd ..
rm -f sources.txt

echo ""
echo "Build successful!"
echo "   JAR: $JAR_NAME ($(du -sh $JAR_NAME | cut -f1))"
echo ""
echo "To run:"
echo "  java -jar $JAR_NAME"
echo ""
echo "Note: Place Oracle JDBC driver at: lib/ojdbc11.jar"
echo "      Download: https://www.oracle.com/database/technologies/appdev/jdbc.html"
