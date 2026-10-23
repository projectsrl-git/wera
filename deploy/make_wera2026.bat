@echo off
setlocal EnableExtensions

REM ================================================================
REM  make_wera2026.bat
REM
REM  Produce i due WAR della nuova interfaccia da affiancare a wera:
REM    target\test\wera2026.war   server di test  (istanza isolata)
REM    target\prod\wera2026.war   server di prod  (dati veri, Quartz spento)
REM
REM  Prerequisito: target\wera.war appena buildato (mvn clean package).
REM  USO:  make_wera2026.bat [war-origine]
REM ================================================================

call "%~dp0make_test_war.bat" "%~1" wera2026 test
if errorlevel 1 exit /b %errorlevel%

call "%~dp0make_test_war.bat" "%~1" wera2026 prod
if errorlevel 1 exit /b %errorlevel%

echo.
echo === Pronti: target\test\wera2026.war e target\prod\wera2026.war
exit /b 0
