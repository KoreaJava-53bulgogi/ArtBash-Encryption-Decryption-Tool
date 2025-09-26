@echo off
setlocal

echo ==================================================
echo      Atbash Cipher Application JAR Builder
echo ==================================================
echo.

echo [1/5] Cleaning up previous build...
del /q *.class >nul 2>&1
del /q AtbashCipherApp.jar >nul 2>&1
del /q manifest.manifest >nul 2>&1
del /q files.list >nul 2>&1
echo Cleanup complete.
echo.

echo [2/5] Compiling Java source files...
REM Add -encoding UTF-8 to correctly handle Korean characters in source files.
javac -encoding UTF-8 *.java
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed. Please check for Java errors.
    goto :error
)
echo Compilation successful.
echo.

echo [3/5] Creating manifest file...
(
    echo Manifest-Version: 1.0
    echo Implementation-Title: ArtBash Encryption/Decryption Tool
    echo Implementation-Version: 5.0
    echo Implementation-Vendor: June
    echo Main-Class: AtbashCipherGUI
) > manifest.manifest
if %errorlevel% neq 0 (
    echo ERROR: Could not create manifest file. Check permissions.
    goto :error
)
echo Manifest created.
echo.

echo [4/5] Creating executable JAR file (AtbashCipherApp.jar)...
(
    dir /b *.class
    dir /b *.json
    REM Only add files that actually exist to prevent 'jar' command errors.
    if exist "README.txt" (echo README.txt)
    if exist "*.png" (dir /b *.png)
) > files.list

jar cfm AtbashCipherApp.jar manifest.manifest @files.list
if %errorlevel% neq 0 (
    echo ERROR: Failed to create JAR file.
    echo Please ensure the 'jar' command is available in your system PATH.
    goto :error
)
echo JAR file created.
echo.

echo [5/5] Cleaning up temporary files...
del /q manifest.manifest >nul 2>&1
del /q files.list >nul 2>&1
echo Temporary files removed.
echo.

echo ==================================================
echo SUCCESS!
echo 'AtbashCipherApp.jar' created.
echo ==================================================
goto :end

:error
echo.
echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
echo                  BUILD FAILED
echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
echo Please check the error messages above.

:end
echo.
pause