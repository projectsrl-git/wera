@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ================================================================
REM  make_test_war.bat
REM
REM  Genera, a partire dal WAR di produzione appena buildato, il WAR
REM  di un'istanza che gira IN PARALLELO a wera (vedi README.md).
REM
REM  Profilo test (istanza isolata, su una COPIA del DB):
REM    - schedulazioni Quartz  -> tasklist azzerata (kill switch)
REM    - import automatico     -> cartelle di I/O isolate
REM    - riscrittura AnlConfig -> path isolato
REM    - invio mail reali      -> SMTP dirottato su mailcatcher locale
REM    - scritture sul DB      -> datasource JNDI separato jdbc/<contesto>
REM    - log applicativi       -> cartella log separata
REM
REM  Profilo prod (nuova interfaccia sui dati veri, accanto a wera):
REM    - schedulazioni Quartz  -> tasklist azzerata: job e mail automatiche
REM                               restano SOLO su wera, niente doppioni
REM    - log applicativi       -> cartella log separata
REM    - DB, SMTP e cartelle   -> INVARIATI (quelli di wera)
REM
REM  NON tocca il repo: lavora su una copia esplosa del WAR.
REM  NON deploya: produce solo l'artefatto.
REM
REM  USO:
REM     make_test_war.bat [war-origine] [contesto] [test|prod]
REM     make_test_war.bat                                  (target\wera.war, wera-test, test)
REM     make_test_war.bat "" wera2026 prod
REM
REM  Output: <cartella del war>\<profilo>\<contesto>.war
REM ================================================================

set "WAR_IN=%~1"
if not defined WAR_IN set "WAR_IN=%~dp0..\target\wera.war"
set "CTX=%~2"
if not defined CTX set "CTX=wera-test"
set "PROFILO=%~3"
if not defined PROFILO set "PROFILO=test"

if /i "%CTX%"=="wera" (
  echo ERRORE: 'wera' e' il contesto di produzione: scegli un altro nome.
  exit /b 1
)
if /i not "%PROFILO%"=="test" if /i not "%PROFILO%"=="prod" (
  echo ERRORE: profilo "%PROFILO%" non valido: usa test oppure prod.
  exit /b 1
)

set "EXT_DIR=/usr/share/webapps/%CTX%/"
set "LOG_DIR=/usr/share/logs/%CTX%/"
set "BUILD=%TEMP%\%CTX%-%PROFILO%-build"

if not exist "%WAR_IN%" (
  echo ERRORE: WAR non trovato: "%WAR_IN%"
  echo         Esegui prima  mvn clean package  oppure passa il path come argomento.
  exit /b 1
)

for %%F in ("%WAR_IN%") do set "WAR_DIR=%%~dpF"
set "OUT_DIR=%WAR_DIR%%PROFILO%"
set "WAR_OUT=%OUT_DIR%\%CTX%.war"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo === [%CTX% / %PROFILO%] Esplodo "%WAR_IN%" ...
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
echo === Patch del config ...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0patch_config_test.ps1" -ConfigPath "%CFG%" -Context "%CTX%" -Profilo "%PROFILO%"
if errorlevel 1 (
  echo ERRORE: patch del config fallita - vedi messaggi sopra.
  exit /b 4
)

echo === Creo le cartelle su questa macchina ...
if /i "%PROFILO%"=="test" (
  for %%D in (import upload output ACS26V3 ANLCONFIG) do (
    if not exist "%~d0\usr\share\webapps\%CTX%\%%D" mkdir "%~d0\usr\share\webapps\%CTX%\%%D" 2>nul
  )
)
REM Log separati: senza la sottocartella crash gli stack trace andrebbero persi.
if not exist "%~d0\usr\share\logs\%CTX%\crash" mkdir "%~d0\usr\share\logs\%CTX%\crash" 2>nul

echo === Riconfeziono "%WAR_OUT%" ...
if exist "%WAR_OUT%" del /f /q "%WAR_OUT%"
REM NON usare PowerShell ZipFile::CreateFromDirectory: su .NET Framework
REM produce entry con separatore backslash (css\animate.css) e Tomcat
REM rifiuta il WAR ("Exception fixing docBase"). jar.exe del JDK genera
REM uno zip conforme (separatore /).
set "JAR_EXE=jar"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_EXE=%JAVA_HOME%\bin\jar.exe"
where "%JAR_EXE%" >nul 2>&1
if errorlevel 1 (
  if not exist "%JAR_EXE%" (
    echo ERRORE: jar.exe non trovato. Imposta JAVA_HOME su un JDK.
    exit /b 5
  )
)
"%JAR_EXE%" -cf "%WAR_OUT%" -C "%BUILD%" .
if errorlevel 1 (
  echo ERRORE: creazione del WAR fallita.
  exit /b 5
)

rmdir /s /q "%BUILD%" 2>nul

echo.
echo ================================================================
echo  OK: "%WAR_OUT%"   [contesto /%CTX%, profilo %PROFILO%]
echo.
echo  Deploy [NON usare -clean: rimuoverebbe la webapp wera]:
echo    deploy_war_tomcat9.bat "%WAR_OUT%" "%CTX%" -catalina "%%CATALINA_HOME%%" -service Tomcat9
echo.
echo  Prima del deploy servono, sul server:
if /i "%PROFILO%"=="test" (
  echo    - conf\Catalina\localhost\%CTX%.xml con la risorsa jdbc/%CTX:-=_% su una COPIA del DB
  echo    - un mailcatcher in ascolto su 127.0.0.1:1025
  echo    - %EXT_DIR% e %LOG_DIR%crash scrivibili da Tomcat
) else (
  echo    - %LOG_DIR%crash scrivibile da Tomcat
  echo    - NESSUN descrittore conf\Catalina\localhost\%CTX%.xml: usa jdbc/wera
  echo      dal META-INF\context.xml del WAR, cioe' il DB di produzione
)
echo  Dettagli in deploy\README.md
echo ================================================================
exit /b 0
