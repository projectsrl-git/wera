<#
================================================================
 patch_config_test.ps1

 Applica a un config.cfg gia' estratto dal WAR gli override che
 rendono l'istanza innocua verso la produzione.

 Invocato da make_test_war.bat. Utilizzabile anche a mano:
   powershell -File patch_config_test.ps1 -ConfigPath "...\config.cfg" -ExtDir "/usr/share/webapps/wera-test/"

 IMPORTANTE: config.cfg e' ISO-8859-1 (codepage 28591). Lettura e
 scrittura sono forzate a quell'encoding: risalvarlo in UTF-8
 corromperebbe gli accenti nelle label.
================================================================
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)] [string] $ConfigPath,
    [Parameter(Mandatory = $true)] [string] $ExtDir
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $ConfigPath)) {
    Write-Error "config.cfg non trovato: $ConfigPath"
    exit 1
}

$enc   = [System.Text.Encoding]::GetEncoding(28591)   # ISO-8859-1
$lines = [System.IO.File]::ReadAllLines($ConfigPath, $enc)

# --- Sostituzioni: chiave -> nuovo valore ------------------------
# La chiave e' confrontata sul prefisso "chiave=" a inizio riga.
$overrides = [ordered]@{
    # [1] Kill switch Quartz.
    #     SchedulerServletApplication.init(): se la tasklist e' vuota
    #     fa return PRIMA di makeScheduler() -> nessun thread Quartz.
    'Quartz-scheduler.tasklist'      = ''

    # [2] Directory di I/O isolate (path assoluti, altrimenti condivisi).
    'directory.external_files'       = $ExtDir
    'cartella.scarico.ACS'           = "${ExtDir}ACS26V3/"
    'cartella.scarico.ACS.webapp'    = 'C:/ACS26V3-TEST/'
    'xmlconfig.ACS'                  = "${ExtDir}ANLCONFIG/AnlConfig.xml"

    # [3] SMTP su mailcatcher locale.
    #     mail.SMTPHost.user DEVE restare non vuoto: SendSMTPMail ha un
    #     ErrDetector.invariant(Util.IsNotEmpty(smtpUser)).
    'mail.SMTPHost'                  = '127.0.0.1'
    'mail.SMTPHost.port'             = '1025'
    'mail.SMTPHost.user'             = 'test'
    'mail.SMTPHost.password'         = 'test'
    'mail.SMTPHost.starttls.enable'  = 'false'
    'mail.from'                      = 'noreply@test.invalid'
    'mail.from.service'              = 'noreply@test.invalid'

    # [4] Datasource separato.
    'DB.ConnectionURL'               = 'java:comp/env/jdbc/wera_test'
    'DB.ConnectionURL.generic'       = 'java:comp/env/jdbc/wera_test'
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

    # [5] Difesa in profondita': commenta le definizioni dei singoli job.
    if ($line -match '^Quartz-scheduler\.job\.') {
        $out.Add("#TEST-OFF# $line")
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

# Il config originale e' a fine-riga LF: WriteAllLines userebbe Environment.NewLine
# (CRLF su Windows) riscrivendo l'intero file. Si forza LF.
[System.IO.File]::WriteAllText($ConfigPath, ($out -join "`n") + "`n", $enc)

# --- Verifica: nessun riferimento attivo alla produzione ---------
$leftovers = @(
    [System.IO.File]::ReadAllLines($ConfigPath, $enc) |
        Where-Object { $_ -match '/usr/share/webapps/wera/' -and $_ -notmatch '^\s*#' }
)
if ($leftovers.Count -gt 0) {
    Write-Host ''
    Write-Host '!! Riferimenti ATTIVI ai path di produzione ancora presenti:' -ForegroundColor Red
    $leftovers | ForEach-Object { Write-Host "   $_" }
    exit 3
}

# --- Riepilogo ---------------------------------------------------
Write-Host ''
Write-Host '===================== VERIFICA ====================='
[System.IO.File]::ReadAllLines($ConfigPath, $enc) |
    Where-Object { $_ -match '^(Quartz-scheduler\.tasklist=|#TEST-OFF#|directory\.external_files=|cartella\.scarico|xmlconfig\.ACS=|mail\.SMTPHost=|mail\.SMTPHost\.port=|mail\.from|DB\.ConnectionURL)' } |
    ForEach-Object { Write-Host "  $_" }
Write-Host '===================================================='
Write-Host '  OK - nessun riferimento attivo ai path di produzione' -ForegroundColor Green
Write-Host ''

exit 0
