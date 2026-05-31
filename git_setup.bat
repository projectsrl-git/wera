@echo off
chcp 65001 >nul
cls
echo ================================================================
echo   WERA - Git Setup Automatico
echo ================================================================
echo.

REM ============================================
REM VERIFICA GIT INSTALLATO
REM ============================================
echo [1/9] Verifico installazione Git...
git --version >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Git non e' installato!
    echo.
    echo Scarica Git da: https://git-scm.com/download/win
    echo Installa e poi ri-esegui questo script.
    echo.
    pause
    exit /b 1
)
echo [OK] Git installato correttamente
echo.

REM ============================================
REM RACCOLTA INFORMAZIONI UTENTE
REM ============================================
echo [2/9] Configurazione Git e GitHub...
echo.
echo REPOSITORY GITHUB:
echo ==================
set /p REPO_OWNER="Proprietario repository (es: projectsrl-git): "
set /p REPO_NAME="Nome repository (es: wera): "
echo.
echo TUO UTENTE GITHUB (chi fa commit/push):
echo ========================================
set /p GITHUB_USER="Username GitHub (es: projectsrl-git): "
set /p GIT_NAME="Nome completo per commit (es: Project SRL): "
set /p GIT_EMAIL="Email per commit (es: info@projectsrl.net): "
echo.
echo RIEPILOGO:
echo ==========
echo Repository:  https://github.com/%REPO_OWNER%/%REPO_NAME%
echo Commit user: %GITHUB_USER% (%GIT_NAME%)
echo Email:       %GIT_EMAIL%
echo Branch:      main
echo.
set /p CONFIRM="Confermi questi dati? (s/n): "
if /i not "%CONFIRM%"=="s" (
    if /i not "%CONFIRM%"=="y" (
        echo Setup annullato. Riesegui lo script.
        pause
        exit /b 0
    )
)
echo.

REM ============================================
REM RESET GIT (RIMUOVE .git SE ESISTE)
REM ============================================
echo [3/9] Reset repository locale...
if exist .git (
    echo Rimuovo vecchio repository Git locale...
    rmdir /s /q .git
    echo [OK] Repository locale resettato
) else (
    echo [INFO] Nessun repository esistente da rimuovere
)
echo.

REM ============================================
REM CREA .gitignore
REM ============================================
echo [4/9] Creazione .gitignore...
(
echo # ========================================
echo # WERA - Git Ignore
echo # ========================================
echo.
echo # === Maven ===
echo target/
echo pom.xml.tag
echo pom.xml.releaseBackup
echo pom.xml.versionsBackup
echo pom.xml.next
echo release.properties
echo dependency-reduced-pom.xml
echo buildNumber.properties
echo .mvn/timing.properties
echo .mvn/wrapper/maven-wrapper.jar
echo.
echo # === IDE ===
echo .idea/
echo *.iws
echo *.iml
echo *.ipr
echo out/
echo .classpath
echo .project
echo .settings/
echo bin/
echo .vscode/
echo *.code-workspace
echo nbproject/private/
echo build/
echo nbbuild/
echo dist/
echo nbdist/
echo .nb-gradle/
echo.
echo # === OS ===
echo .DS_Store
echo .DS_Store?
echo ._*
echo .Spotlight-V100
echo .Trashes
echo Thumbs.db
echo ehthumbs.db
echo Desktop.ini
echo $RECYCLE.BIN/
echo *~
echo .directory
echo.
echo # === Application ===
echo logs/
echo *.log
echo *.log.*
echo uploads/
echo !uploads/.gitkeep
echo *.tmp
echo *.temp
echo *.swp
echo *.swo
echo.
echo # === Database ===
echo *.db
echo *.mv.db
echo *.trace.db
echo.
echo # === Spring Boot ===
echo .spring-boot-devtools.properties
echo.
echo # === Security ===
echo application-prod.properties
echo application-secret.properties
echo *.key
echo *.pem
echo *.p12
echo *.jks
echo.
echo # === Build ===
echo *.jar
echo *.war
echo *.ear
echo *.class
echo.
echo # === Other ===
echo *.pid
echo *.seed
echo *.pid.lock
echo node_modules/
) > .gitignore
echo [OK] .gitignore creato
echo.

REM ============================================
REM CONTROLLO SEGRETI (repo pubblico!)
REM ============================================
echo [5/9] Controllo segreti...
echo.
echo [ATTENZIONE] Il repository e' PUBBLICO.
echo Il file src\main\resources\application.properties (o application.yml)
echo di solito contiene la stringa di connessione al database, con utente
echo e password. Se viene caricato, sara' visibile a chiunque.
echo.
set /p HIDE_PROPS="Vuoi ESCLUDERE application.properties / application.yml dal commit? (s/n): "
if /i "%HIDE_PROPS%"=="s" goto :hide_props
if /i "%HIDE_PROPS%"=="y" goto :hide_props
echo [INFO] application.properties verra' incluso. Verifica TU che non contenga segreti!
goto :props_done
:hide_props
(
echo.
echo # === Config locale con segreti (escluso dal repo pubblico) ===
echo src/main/resources/application.properties
echo src/main/resources/application.yml
) >> .gitignore
echo [OK] application.properties / application.yml esclusi dal commit
echo      Suggerimento: committa una versione esempio senza segreti,
echo      es. application.properties.example, per i collaboratori.
:props_done
echo.

REM ============================================
REM INIZIALIZZA GIT
REM ============================================
echo [6/9] Inizializzazione repository Git...
git init
if errorlevel 1 (
    echo [ERRORE] Errore durante git init
    pause
    exit /b 1
)
git config user.name "%GIT_NAME%"
git config user.email "%GIT_EMAIL%"
echo [OK] Repository inizializzato e configurato
echo    Nome:  %GIT_NAME%
echo    Email: %GIT_EMAIL%
echo.

REM ============================================
REM ADD E COMMIT
REM ============================================
echo [7/9] Aggiunta file e commit...
git add .
if errorlevel 1 (
    echo [ERRORE] Errore durante git add
    pause
    exit /b 1
)
echo [OK] File aggiunti
echo.
echo Verifica file da committare...
git status --short
echo.

echo Initial commit - WERA > .git\COMMIT_MSG
echo. >> .git\COMMIT_MSG
echo Caricamento iniziale del progetto WERA. >> .git\COMMIT_MSG

git commit -F .git\COMMIT_MSG
if errorlevel 1 (
    echo [ERRORE] Errore durante git commit
    pause
    exit /b 1
)
echo [OK] Commit creato
echo.

REM ============================================
REM RINOMINA BRANCH IN main
REM ============================================
echo [8/9] Imposto branch 'main' e remote...
git branch -M main
git remote add origin https://github.com/%REPO_OWNER%/%REPO_NAME%.git
if errorlevel 1 (
    echo [AVVISO] Remote gia' esistente, lo rimuovo e ricreo...
    git remote remove origin
    git remote add origin https://github.com/%REPO_OWNER%/%REPO_NAME%.git
)
echo [OK] Branch 'main' e remote configurati
echo    Repository: https://github.com/%REPO_OWNER%/%REPO_NAME%
echo.

REM ============================================
REM SALVA CONFIGURAZIONE PER git_push.bat
REM ============================================
echo [9/9] Salvataggio configurazione...
(
echo set REPO_OWNER=%REPO_OWNER%
echo set REPO_NAME=%REPO_NAME%
echo set GITHUB_USER=%GITHUB_USER%
echo set GIT_NAME=%GIT_NAME%
echo set GIT_EMAIL=%GIT_EMAIL%
) > .git\config.bat
echo [OK] Configurazione salvata in .git\config.bat
echo.

echo ================================================================
echo   PRONTO PER IL PUSH!
echo ================================================================
echo.
echo Repository:  https://github.com/%REPO_OWNER%/%REPO_NAME%
echo Push come:   %GITHUB_USER%
echo Branch:      main
echo.
echo [IMPORTANTE] Il repo GitHub deve gia' esistere ed essere VUOTO
echo              (nessun README). Al push usa:
echo                Username: %GITHUB_USER%
echo                Password: [IL TUO TOKEN GitHub]
echo              Crea il token su: https://github.com/settings/tokens
echo.
echo ================================================================
echo.

set /p DO_PUSH="Vuoi fare il PUSH adesso? (s/n): "
if /i "%DO_PUSH%"=="s" goto :do_push
if /i "%DO_PUSH%"=="y" goto :do_push
goto :end

:do_push
echo.
echo ================================================================
echo   PUSH IN CORSO...
echo ================================================================
echo.
echo Username: %GITHUB_USER%   Password: [IL TUO TOKEN]
echo.
git push -u origin main
if errorlevel 1 (
    echo.
    echo [ERRORE] Errore durante il push!
    echo.
    echo Possibili cause:
    echo   1. Token errato o scaduto
    echo   2. Repository %REPO_OWNER%/%REPO_NAME% non esistente
    echo   3. Repository NON vuoto (es. creato con README) -^> push rifiutato
    echo.
    echo Come risolvere:
    echo   - Verifica che il repo esista e sia vuoto:
    echo     https://github.com/%REPO_OWNER%/%REPO_NAME%
    echo   - Se contiene gia' un README, ricrealo vuoto su https://github.com/new
    echo   - Rigenera il token: https://github.com/settings/tokens
    echo   - Riprova: git push -u origin main
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
echo Verifica che NON ci sia la cartella target/ e che NON ci siano segreti.
echo.
goto :end

:end
echo.
echo ================================================================
echo   SETUP COMPLETATO!
echo ================================================================
echo.
echo COMANDI UTILI:
echo   git status          - Stato repository
echo   git log --oneline   - Lista commit
echo   git push            - Carica su GitHub
echo   git_push.bat        - Helper push automatico
echo.
pause
