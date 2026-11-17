@echo off
setlocal
echo ===================================================
echo  Warehouse Inventory & Supplier Management System
echo  Compiling and Launching Application...
echo ===================================================

if not exist bin mkdir bin

:: Find modern Java if available
set JAVA_CMD=java
set JAVAC_CMD=javac

if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Java\jdk-26.0.2\bin\javac.exe"
)

echo [1/2] Compiling Java source files...
"%JAVAC_CMD%" -cp "lib\gson-2.10.1.jar" -d bin src\models\*.java src\exceptions\*.java src\utils\*.java src\storage\*.java src\services\*.java src\ui\*.java src\main\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please check Java errors above.
    pause
    exit /b %errorlevel%
)

echo [2/2] Launching Desktop GUI...
"%JAVA_CMD%" -cp "bin;lib\gson-2.10.1.jar" main.Main
pause

