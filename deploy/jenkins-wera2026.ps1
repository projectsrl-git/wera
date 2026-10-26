# ============================================================
# deploy/jenkins-wera2026.ps1 - lancia il job Jenkins "wera2026 PROD" (192.168.0.115) e ne segue la console.
# ------------------------------------------------------------
# Credenziali: file con UNA riga "utente:tokenAPI" (utente dedicato, es. cli-deploy, permessi Read/Build
# sul solo job), FUORI dal repo. Default: %USERPROFILE%\.wera\jenkins-cli-deploy.txt (parametro -Credenziali).
# Il token non viene mai stampato ne' scritto altrove.
#
# Uso (Windows PowerShell 5.1):
#   .\deploy\jenkins-wera2026.ps1                 # DEPLOY=false: solo build e artefatto patchato
#   .\deploy\jenkins-wera2026.ps1 -Deploy         # DEPLOY=true: build + deploy a caldo su .106
#   .\deploy\jenkins-wera2026.ps1 -Branch xyz     # branch diversa da release/wera2026 (eccezione)
# Uscita: 0 se la build finisce SUCCESS, 1 altrimenti.
# Vincolo di rete: la build scarica da GitHub/Maven -> lanciarla fra le 06:00 e le 23:00 (FortiGate).
# ============================================================
param(
    [switch]$Deploy,
    [string]$Branch = "release/wera2026",
    [string]$Credenziali = (Join-Path $env:USERPROFILE ".wera\jenkins-cli-deploy.txt"),
    [string]$Jenkins = "http://192.168.0.115:8080",
    [int]$TimeoutMinuti = 30
)

$ErrorActionPreference = "Stop"
$job = "wera2026%20PROD"

if (-not (Test-Path $Credenziali)) { Write-Error "File delle credenziali non trovato: $Credenziali"; exit 1 }
$riga = (Get-Content -Raw $Credenziali).Trim()
if ($riga -notmatch '^[^:\s]+:\S+$') { Write-Error "Formato atteso nel file: utente:token (una riga)"; exit 1 }
$auth = "Basic " + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($riga))
$h = @{ Authorization = $auth }

function Chiama([string]$metodo, [string]$url) {
    # Invoke-WebRequest senza redirect automatici: serve l'header Location della coda
    return Invoke-WebRequest -Uri $url -Method $metodo -Headers $h -UseBasicParsing -MaximumRedirection 0 -ErrorAction Stop
}

$valDeploy = if ($Deploy) { "true" } else { "false" }
Write-Host ("Lancio 'wera2026 PROD' BRANCH={0} DEPLOY={1}" -f $Branch, $valDeploy)
$url = "$Jenkins/job/$job/buildWithParameters?BRANCH=" + [Uri]::EscapeDataString($Branch) + "&DEPLOY=$valDeploy"
try { $r = Chiama "POST" $url } catch { Write-Error ("Lancio rifiutato: " + $_.Exception.Message); exit 1 }
$coda = $r.Headers["Location"]
if (-not $coda) { Write-Error "Jenkins non ha restituito l'elemento in coda"; exit 1 }
Write-Host "In coda: $coda"

# attesa del numero di build
$numero = $null
$limite = (Get-Date).AddMinutes($TimeoutMinuti)
while (-not $numero) {
    if ((Get-Date) -gt $limite) { Write-Error "Timeout in coda"; exit 1 }
    Start-Sleep -Seconds 3
    $q = (Chiama "GET" ($coda.TrimEnd('/') + "/api/json")).Content | ConvertFrom-Json
    if ($q.cancelled) { Write-Error "Elemento in coda annullato"; exit 1 }
    if ($q.executable) { $numero = $q.executable.number }
}
Write-Host "Build #$numero"

# console progressiva
$inizio = 0
do {
    if ((Get-Date) -gt $limite) { Write-Error "Timeout della build"; exit 1 }
    $c = Chiama "GET" "$Jenkins/job/$job/$numero/logText/progressiveText?start=$inizio"
    if ($c.Content) { Write-Host -NoNewline $c.Content }
    $inizio = [int]$c.Headers["X-Text-Size"]
    $altro = $c.Headers["X-More-Data"] -eq "true"
    if ($altro) { Start-Sleep -Seconds 3 }
} while ($altro)

$b = (Chiama "GET" "$Jenkins/job/$job/$numero/api/json").Content | ConvertFrom-Json
Write-Host ""
Write-Host ("Esito build #{0}: {1}" -f $numero, $b.result)
if ($b.result -eq "SUCCESS") { exit 0 } else { exit 1 }
