@echo off
rem -------------------------------------------------------------------------
rem Bootstrap Script for Windows
rem -------------------------------------------------------------------------

@if not "%ECHO%" == ""  echo %ECHO%
setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

:MAIN
rem $Id$
)

pushd "%DIRNAME%.."
set "RESOLVED_APP_HOME=%CD%"
popd

if "x%APP_HOME%" == "x" (
  set "APP_HOME=%RESOLVED_APP_HOME%"
)

pushd "%APP_HOME%"
set "SANITIZED_APP_HOME=%CD%"
popd

if /i "%RESOLVED_APP_HOME%" NEQ "%SANITIZED_APP_HOME%" (
   echo.
   echo   WARNING:  APP_HOME may be pointing to a different installation - unpredictable results may occur.
   echo.
   echo       APP_HOME: "%APP_HOME%"
   echo.
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  if not exist "%JAVA_HOME%" (
    echo JAVA_HOME "%JAVA_HOME%" path doesn't exist
    goto END
   ) else (
     if not exist "%JAVA_HOME%\bin\java.exe" (
       echo "%JAVA_HOME%\bin\java.exe" does not exist
       goto END_NO_PAUSE
     )
      echo Setting JAVA property to "%JAVA_HOME%\bin\java"
    set "JAVA=%JAVA_HOME%\bin\java"
  )
)

"%JAVA%" --add-modules=java.se -version >nul 2>&1 && (set MODULAR_JDK=true) || (set MODULAR_JDK=false)

if not "%PRESERVE_JAVA_OPTS%" == "true" (
  rem Add -client to the JVM options, if supported (32 bit VM), and not overriden
  echo "%JAVA_OPTS%" | findstr /I \-server > nul
  if errorlevel == 1 (
    "%JAVA%" -client -version 2>&1 | findstr /I /C:"Client VM" > nul
    if not errorlevel == 1 (
      set "JAVA_OPTS=-client %JAVA_OPTS%"
    )
  )
)

rem Find quarkus-run.jar, or we can't continue
if exist "%APP_HOME%\quarkus-run.jar" (
    set "RUNJAR=%APP_HOME%\quarkus-run.jar"
) else (
  echo Could not locate "%APP_HOME%\quarkus-run.jar".
  echo Please check that you are in the bin directory when running this script.
  goto END
)

rem Setup JBoss specific properties

rem Setup directories, note directories with spaces do not work
setlocal EnableDelayedExpansion
set "CONSOLIDATED_OPTS=%JAVA_OPTS% %SERVER_OPTS%"
set baseDirFound=false
set configDirFound=false
set logDirFound=false
for %%a in (!CONSOLIDATED_OPTS!) do (
   if !baseDirFound! == true (
      set "APP_BASE_DIR=%%~a"
      set baseDirFound=false
   )
)
setlocal DisableDelayedExpansion

rem Set the standalone base dir
if "x%APP_BASE_DIR%" == "x" (
  set  "APP_BASE_DIR=%APP_HOME%\standalone"
)

echo ===============================================================================
echo.
echo   Bootstrap Environment
echo.
echo   APP_HOME: "%APP_HOME%"
echo.
echo   JAVA: "%JAVA%"
echo.
echo ===============================================================================
echo.

cd "%APP_HOME%"

:RESTART
  "%JAVA%" %JAVA_OPTS% ^
      -jar "%APP_HOME%\quarkus-run.jar"

if %errorlevel% equ 10 (
	goto RESTART
)

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
