@echo off
setlocal
echo ===================================================
echo  Compiling Warehouse Inventory Management System
echo ===================================================

if not exist bin mkdir bin

set JAVAC_CMD=javac
if exist "C:\Program Files\Java\jdk-26.0.2\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-26.0.2\bin\javac.exe"
)

echo Compiling Java source files...
"%JAVAC_CMD%" -cp "lib\gson-2.10.1.jar" -d bin src\models\*.java src\exceptions\*.java src\utils\*.java src\storage\*.java src\services\*.java src\ui\*.java src\main\*.java
if %errorlevel% equ 0 (
    echo [SUCCESS] Compilation successful! Class files are in the 'bin' folder.
) else (
    echo [ERROR] Compilation failed.
)
pause

