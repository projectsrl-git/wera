<#
================================================================
 patch_config_test.ps1

 Applica a un config.cfg gia' estratto dal WAR gli override per
 un'istanza che gira IN PARALLELO a quella di produzione (wera).

 Profili:
   test  istanza isolata: Quartz spento, cartelle di I/O e AnlConfig
         separati, SMTP su mailcatcher locale, datasource separato,
         log separati. Per collaudare su una COPIA del DB.
   prod  stessa produzione, nuova interfaccia: stesso DB e cartelle di
         wera (gli utenti modificano dati e caricano file veri), ma
         Quartz SPENTO (job restano solo su wera), NESSUNA mail reale
         (SMTP sul mailcatcher locale, come nel profilo test) e log
         separati.

 Invocato da make_test_war.bat. Utilizzabile anche a mano:
   powershell -File patch_config_test.ps1 -ConfigPath "...\config.cfg" -Context wera2026 -Profilo test

 IMPORTANTE: config.cfg e' ISO-8859-1 (codepage 28591). Lettura e
 scrittura sono forzate a quell'encoding: risalvarlo in UTF-8
 corromperebbe gli accenti nelle label.
================================================================
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)] [string] $ConfigPath,
    [string] $Context = 'wera-test',
    [ValidateSet('test', 'prod')] [string] $Profilo = 'test'
)

$ErrorActionPreference = 'Stop'

if ($Context -eq 'wera') {
    Write-Error "Il contesto 'wera' e' quello di produzione: scegli un altro nome."
    exit 1
}
if (-not (Test-Path -LiteralPath $ConfigPath)) {
    Write-Error "config.cfg non trovato: $ConfigPath"
    exit 1
}

$ExtDir = "/usr/share/webapps/$Context/"
$LogDir = "/usr/share/logs/$Context/"
# jdbc/wera_test per wera-test (compatibile con i descrittori esistenti), jdbc/wera2026 per wera2026
$DsName = 'jdbc/' + ($Context -replace '-', '_')
$Marker = '#' + $Profilo.ToUpper() + '-OFF# '

$enc   = [System.Text.Encoding]::GetEncoding(28591)   # ISO-8859-1
$raw   = [System.IO.File]::ReadAllText($ConfigPath, $enc)
# Si conservano i fine-riga dell'originale (oggi CRLF): il file patchato deve
# differire dall'originale solo nelle righe davvero cambiate.
$nl    = if ($raw.Contains("`r`n")) { "`r`n" } else { "`n" }
$lines = [System.IO.File]::ReadAllLines($ConfigPath, $enc)

# --- Sostituzioni: chiave -> nuovo valore ------------------------
# La chiave e' confrontata sul prefisso "chiave=" a inizio riga.
$overrides = [ordered]@{
    # [1] Kill switch Quartz (entrambi i profili).
    #     SchedulerServletApplication.init(): se la tasklist e' vuota
    #     fa return PRIMA di makeScheduler() -> nessun thread Quartz.
    'Quartz-scheduler.tasklist'      = ''

    # [2] Log applicativi separati (entrambi i profili): con i path di
    #     produzione le due istanze scriverebbero negli stessi file.
    'Logger.CrashDir'                = "${LogDir}crash/wera_crash"
    'Logger.DebugFile'               = "${LogDir}wera_debug"
    'Logger.ApplicationLog'          = "${LogDir}wera_application.log"

    # [3] SMTP su mailcatcher locale (entrambi i profili): nessuna mail
    #     reale parte da un'istanza parallela.
    #     mail.SMTPHost.user DEVE restare non vuoto: SendSMTPMail ha un
    #     ErrDetector.invariant(Util.IsNotEmpty(smtpUser)).
    'mail.SMTPHost'                  = '127.0.0.1'
    'mail.SMTPHost.port'             = '1025'
    'mail.SMTPHost.user'             = 'test'
    'mail.SMTPHost.password'         = 'test'
    'mail.SMTPHost.starttls.enable'  = 'false'
    'mail.from'                      = 'noreply@test.invalid'
    'mail.from.service'              = 'noreply@test.invalid'
}

if ($Profilo -eq 'test') {
    # [4] Directory di I/O isolate (path assoluti, altrimenti condivisi).
    $overrides['directory.external_files']      = $ExtDir
    $overrides['cartella.scarico.ACS']          = "${ExtDir}ACS26V3/"
    $overrides['cartella.scarico.ACS.webapp']   = 'C:/ACS26V3-TEST/'
    $overrides['xmlconfig.ACS']                 = "${ExtDir}ANLCONFIG/AnlConfig.xml"

    # [5] Datasource separato (descrittore conf\Catalina\localhost\<contesto>.xml).
    $overrides['DB.ConnectionURL']              = "java:comp/env/$DsName"
    $overrides['DB.ConnectionURL.generic']      = "java:comp/env/$DsName"
}

$applied = @{}
foreach ($k in $overrides.Keys) { $applied[$k] = $false }

$out = New-Object System.Collections.Generic.List[string]

foreach ($line in $lines) {

    $matched = $false

    foreach ($key in $overrides.Keys) {
        if ($line.StartsWith("$key=")) {
            $out.Add("$key=$($overrides[$key])")
            $applied[$key] = $true
            $matched = $true
            break
        }
    }

    if ($matched) { continue }

    # [6] Difesa in profondita': commenta le definizioni dei singoli job.
    if ($line -match '^Quartz-scheduler\.job\.') {
        $out.Add("$Marker$line")
        continue
    }

    $out.Add($line)
}

# --- Nessuna chiave puo' mancare ---------------------------------
$missing = @($applied.Keys | Where-Object { -not $applied[$_] })
if ($missing.Count -gt 0) {
    Write-Error ("Chiavi non trovate in config.cfg: " + ($missing -join ', ') + `
                 "`nIl config e' cambiato: aggiorna questo script prima di deployare.")
    exit 2
}

[System.IO.File]::WriteAllText($ConfigPath, ($out -join $nl) + $nl, $enc)

# --- Verifica: nessun riferimento attivo a cio' che va isolato ---
# test: niente cartelle ne' log di produzione. prod: niente log di produzione
# (le cartelle di wera sono condivise di proposito).
$vietati = if ($Profilo -eq 'test') { '/usr/share/(webapps|logs)/wera/' } else { '/usr/share/logs/wera/' }
$result  = [System.IO.File]::ReadAllLines($ConfigPath, $enc)
$leftovers = @($result | Where-Object { $_ -match $vietati -and $_ -notmatch '^\s*#' })
$tasklist  = @($result | Where-Object { $_ -match '^Quartz-scheduler\.tasklist=\s*\S' })
$smtp      = @($result | Where-Object { $_ -match '^mail\.SMTPHost=' -and $_ -ne 'mail.SMTPHost=127.0.0.1' })
if ($leftovers.Count -gt 0 -or $tasklist.Count -gt 0 -or $smtp.Count -gt 0) {
    Write-Host ''
    Write-Host '!! Riferimenti ATTIVI che dovevano essere isolati:' -ForegroundColor Red
    ($leftovers + $tasklist + $smtp) | ForEach-Object { Write-Host "   $_" }
    exit 3
}

# --- Riepilogo ---------------------------------------------------
Write-Host ''
Write-Host "============ VERIFICA  contesto=$Context  profilo=$Profilo ============"
$result |
    Where-Object { $_ -match '^(Quartz-scheduler\.tasklist=|#(TEST|PROD)-OFF#|directory\.external_files=|cartella\.scarico|xmlconfig\.ACS=|mail\.SMTPHost=|mail\.SMTPHost\.port=|mail\.from|DB\.ConnectionURL|Logger\.(CrashDir|DebugFile|ApplicationLog)=)' } |
    ForEach-Object { Write-Host "  $_" }
Write-Host '======================================================================'
Write-Host "  OK - Quartz spento, SMTP su 127.0.0.1, nessun riferimento attivo a $vietati" -ForegroundColor Green
Write-Host ''

exit 0
