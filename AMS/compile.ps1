# PowerShell build script for AMS
$SrcDir = "src"
$OutDir = "out"
$LibDir = "lib"
$JarName = "AMS.jar"
$MainClass = "com.ams.Main"

# Create output directory
if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir | Out-Null
}

# Create lib directory if not exists
if (-not (Test-Path $LibDir)) {
    New-Item -ItemType Directory -Path $LibDir | Out-Null
}

# Get all Java files
$JavaFiles = Get-ChildItem -Path $SrcDir -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

# Check for Oracle JDBC
$ClassPath = ""
if (Test-Path "$LibDir\ojdbc8.jar") {
    $ClassPath = "$LibDir\ojdbc8.jar"
} elseif (Test-Path "$LibDir\ojdbc.jar") {
    $ClassPath = "$LibDir\ojdbc.jar"
}

# Compile
Write-Host "Compiling Java sources..."
if ($ClassPath) {
    & javac -encoding UTF-8 -d $OutDir -cp $ClassPath -sourcepath $SrcDir $JavaFiles
} else {
    & javac -encoding UTF-8 -d $OutDir -sourcepath $SrcDir $JavaFiles
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
    
    # Create manifest
    @"
Manifest-Version: 1.0
Main-Class: $MainClass
Class-Path: lib/ojdbc8.jar lib/ojdbc.jar
"@ | Out-File -FilePath "$OutDir\manifest.mf" -Encoding ASCII
    
    # Create JAR
    Push-Location $OutDir
    & jar cfm "..\$JarName" manifest.mf .
    Pop-Location
    
    Write-Host "JAR created: $JarName"
} else {
    Write-Host "Compilation failed!"
    exit 1
}
