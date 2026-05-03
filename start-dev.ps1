$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $Root "backend"
$FrontendDir = Join-Path $Root "frontend"
$BackendUrl = "http://localhost:8080/api/auth/login"
$FrontendUrl = "http://localhost:5173"

function Test-Command($Name) {
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Test-HttpReady($Url) {
    try {
        Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 2 -UseBasicParsing | Out-Null
        return $true
    } catch {
        if ($_.Exception.Response -ne $null) {
            return $true
        }
        return $false
    }
}

Write-Host "Dental Clinic dev launcher" -ForegroundColor Cyan
Write-Host "Root: $Root"

if (-not (Test-Path (Join-Path $BackendDir "mvnw.cmd"))) {
    throw "Backend Maven wrapper not found: $BackendDir"
}

if (-not (Test-Path (Join-Path $FrontendDir "package.json"))) {
    throw "Frontend package.json not found: $FrontendDir"
}

if (-not (Test-Command "node")) {
    throw "Node.js is not available in PATH."
}

if (-not (Test-Command "npm")) {
    throw "npm is not available in PATH."
}

if (-not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
    Write-Host "Frontend dependencies are missing. Running npm install first..." -ForegroundColor Yellow
    Push-Location $FrontendDir
    npm install
    Pop-Location
}

Write-Host "Starting backend first..." -ForegroundColor Green
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-Command",
    "cd '$BackendDir'; .\mvnw.cmd spring-boot:run"
) -WorkingDirectory $BackendDir

Write-Host "Waiting for backend on http://localhost:8080 ..." -ForegroundColor Yellow
$BackendReady = $false
for ($i = 1; $i -le 90; $i++) {
    if (Test-HttpReady $BackendUrl) {
        $BackendReady = $true
        break
    }
    Start-Sleep -Seconds 2
    Write-Host "." -NoNewline
}
Write-Host ""

if (-not $BackendReady) {
    throw "Backend did not become ready within 180 seconds. Check the backend terminal."
}

Write-Host "Backend is ready. Starting frontend..." -ForegroundColor Green
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-Command",
    "cd '$FrontendDir'; npm run dev"
) -WorkingDirectory $FrontendDir

Write-Host "Startup sequence complete." -ForegroundColor Cyan
Write-Host "Backend:  http://localhost:8080"
Write-Host "Frontend: $FrontendUrl"
