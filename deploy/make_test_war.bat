@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ================================================================
REM  make_test_war.bat
REM
REM  Genera wera-test.war a partire dal WAR di produzione appena
REM  buildato, neutralizzando tutto cio' che puo' interferire con
REM  l'istanza principale:
REM
REM    - schedulazioni Quartz  -> tasklist azzerata (kill switch)
REM    - import automatico     -> cartelle di I/O isolate
REM    - riscrittura AnlConfig -> path isolato
REM    - invio mail reali      -> SMTP dirottato su mailcatcher locale
REM    - scritture sul DB      -> datasource JNDI separato
REM
REM  NON tocca il repo: lavora su una copia esplosa del WAR.
REM  NON deploya: produce solo l'artefatto (vedi README.md).
REM
REM  USO:
REM     make_test_war.bat
REM     make_test_war.bat "D:\SVILUPPO\wera_git\target\wera.war"
REM
REM  Output: <cartella del war>\wera-test.war
REM ================================================================

set "WAR_IN=%~1"
if not defined WAR_IN set "WAR_IN=%~dp0..\target\wera.war"

set "CTX=wera-test"
set "EXT_DIR=/usr/share/webapps/%CTX%/"
set "BUILD=%TEMP%\%CTX%-build"

if not exist "%WAR_IN%" (
  echo ERRORE: WAR non trovato: "%WAR_IN%"
  echo         Esegui prima  mvn clean package  oppure passa il path come argomento.
  exit /b 1
)

for %%F in ("%WAR_IN%") do set "WAR_DIR=%%~dpF"
set "WAR_OUT=%WAR_DIR%%CTX%.war"

echo === Esplodo "%WAR_IN%" ...
if exist "%BUILD%" rmdir /s /q "%BUILD%"
mkdir "%BUILD%"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Add-Type -AssemblyName System.IO.Compression.FileSystem; [IO.Compression.ZipFile]::ExtractToDirectory('%WAR_IN%','%BUILD%')"
if errorlevel 1 (
  echo ERRORE: estrazione del WAR fallita.
  exit /b 2
)

set "CFG=%BUILD%\WEB-INF\config\config.cfg"
if not exist "%CFG%" (
  echo ERRORE: WEB-INF\config\config.cfg non trovato nel WAR.
  exit /b 3
)

REM ================================================================
REM  Patch del config.
REM  config.cfg e' ISO-8859-1: lettura e scrittura forzate a 28591,
REM  altrimenti gli accenti vengono corrotti.
REM ================================================================
echo === Patch del config per l'istanza di test ...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0patch_config_test.ps1" -ConfigPath "%CFG%" -ExtDir "%EXT_DIR%"
if errorlevel 1 (
  echo ERRORE: patch del config fallita - vedi messaggi sopra.
  exit /b 4
)

echo === Creo le cartelle isolate ...
for %%D in (import upload output ACS26V3 ANLCONFIG) do (
  if not exist "%~d0\usr\share\webapps\%CTX%\%%D" mkdir "%~d0\usr\share\webapps\%CTX%\%%D" 2>nul
)

echo === Riconfeziono "%WAR_OUT%" ...
if exist "%WAR_OUT%" del /f /q "%WAR_OUT%"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Add-Type -AssemblyName System.IO.Compression.FileSystem; [IO.Compression.ZipFile]::CreateFromDirectory('%BUILD%','%WAR_OUT%')"
if errorlevel 1 (
  echo ERRORE: creazione del WAR fallita.
  exit /b 5
)

rmdir /s /q "%BUILD%" 2>nul

echo.
echo ================================================================
echo  OK: "%WAR_OUT%"
echo.
echo  Deploy [NON usare -clean: rimuoverebbe la webapp wera]:
echo    deploy_war_tomcat9.bat "%WAR_OUT%" "%CTX%" -catalina "%%CATALINA_HOME%%" -service Tomcat9
echo.
echo  Prima del deploy servono:
echo    - conf\Catalina\localhost\%CTX%.xml con la risorsa jdbc/wera_test
echo    - un mailcatcher in ascolto su 127.0.0.1:1025
echo  Dettagli in deploy\README.md
echo ================================================================
exit /b 0
