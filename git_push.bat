@echo off
chcp 65001 >nul
cls
echo ================================================================
echo   WERA - Git Push Helper
echo ================================================================
echo.

REM ============================================
REM VERIFICA GIT CONFIGURATO
REM ============================================
if not exist .git (
    echo [ERRORE] Repository Git non inizializzato!
    echo Esegui prima: git_setup.bat
    echo.
    pause
    exit /b 1
)

git remote -v | findstr origin >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Remote 'origin' non configurato!
    echo Esegui prima: git_setup.bat
    echo.
    pause
    exit /b 1
)

echo [OK] Repository Git configurato
echo.

REM ============================================
REM CARICA CONFIGURAZIONE SALVATA
REM ============================================
if exist .git\config.bat (
    call .git\config.bat
    echo [OK] Configurazione caricata
) else (
    echo [AVVISO] config.bat non trovato, estraggo da git config...
    for /f "tokens=*" %%i in ('git config user.name') do set GIT_NAME=%%i
    for /f "tokens=*" %%i in ('git config user.email') do set GIT_EMAIL=%%i
    for /f "tokens=*" %%i in ('git remote get-url origin') do set REMOTE_URL=%%i
    for /f "tokens=4,5 delims=/" %%i in ("%REMOTE_URL%") do (
        set REPO_OWNER=%%i
        set REPO_NAME=%%j
    )
    set REPO_NAME=%REPO_NAME:.git=%
    set /p GITHUB_USER="Username GitHub per il push: "
)
echo.

REM ============================================
REM INFO REPOSITORY
REM ============================================
echo INFORMAZIONI REPOSITORY:
echo   Repository:  https://github.com/%REPO_OWNER%/%REPO_NAME%
echo   Commit user: %GIT_NAME% ^<%GIT_EMAIL%^>
echo   Push user:   %GITHUB_USER%
echo   Branch:      main
echo.

REM ============================================
REM MODIFICHE DA PUSHARE
REM ============================================
echo MODIFICHE DA PUSHARE:
git log --oneline origin/main..HEAD 2>nul
echo.

REM ============================================
REM CONFERMA
REM ============================================
set /p CONFIRM="Vuoi procedere con il PUSH? (s/n): "
if /i not "%CONFIRM%"=="s" (
    if /i not "%CONFIRM%"=="y" (
        echo Push annullato.
        pause
        exit /b 0
    )
)

echo.
echo ================================================================
echo   PUSH IN CORSO...
echo ================================================================
echo.
echo Se richiesto:  Username: %GITHUB_USER%   Password: [IL TUO TOKEN]
echo.

git push origin main
if errorlevel 1 (
    echo.
    echo [ERRORE] Errore durante il push!
    echo Possibili cause: token errato/scaduto, repo inesistente,
    echo oppure il remote ha commit che non hai in locale.
    echo.
    echo Se il remote e' avanti, allinea prima con:
    echo   git pull --rebase origin main
    echo e poi riprova:
    echo   git push origin main
    echo.
    pause
    exit /b 1
)

echo.
echo ================================================================
echo   PUSH COMPLETATO CON SUCCESSO!
echo ================================================================
echo.
echo Repository: https://github.com/%REPO_OWNER%/%REPO_NAME%
echo.
pause
