param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbName = "dental_clinic",
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    [switch]$SkipDatabaseInit,
    [switch]$SkipFrontendInstall
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $ScriptDir "..\..")).Path
$BackendDir = Join-Path $Root "backend"
$FrontendDir = Join-Path $Root "frontend"
$SchemaFile = Join-Path $BackendDir "src\main\resources\schema.sql"
$DataFile = Join-Path $BackendDir "src\main\resources\data.sql"
$BackendReadyUrl = "http://localhost:8080/api/auth/login"
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

function Get-MySqlArgs([switch]$WithDatabase) {
    $args = @(
        "--default-character-set=utf8mb4",
        "-h", $DbHost,
        "-P", $DbPort.ToString(),
        "-u", $DbUser
    )
    if ($DbPassword) {
        $args += "-p$DbPassword"
    }
    if ($WithDatabase) {
        $args += $DbName
    }
    return $args
}

function Invoke-MySqlServerStatement($Statement) {
    & mysql @(Get-MySqlArgs) -e $Statement
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed: $Statement"
    }
}

function Invoke-MySqlDatabaseScript($FilePath) {
    $SourcePath = $FilePath.Replace("\", "/")
    & mysql @(Get-MySqlArgs -WithDatabase) -e "SOURCE $SourcePath;"
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL import failed: $FilePath"
    }
}

function Start-VisibleProcess($WorkingDirectory, $CommandText) {
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-Command", $CommandText
    ) -WorkingDirectory $WorkingDirectory
}

Write-Host "Dental Clinic bootstrap launcher" -ForegroundColor Cyan
Write-Host "Workspace: $Root"

if (-not (Test-Path (Join-Path $BackendDir "mvnw.cmd"))) {
    throw "Backend Maven wrapper not found: $BackendDir"
}

if (-not (Test-Path (Join-Path $FrontendDir "package.json"))) {
    throw "Frontend package.json not found: $FrontendDir"
}

if (-not (Test-Path $SchemaFile)) {
    throw "schema.sql not found: $SchemaFile"
}

if (-not (Test-Path $DataFile)) {
    throw "data.sql not found: $DataFile"
}

foreach ($command in @("java", "node", "npm", "mysql")) {
    if (-not (Test-Command $command)) {
        throw "$command is not available in PATH."
    }
}

if (-not $SkipDatabaseInit) {
    Write-Host "Initializing MySQL database..." -ForegroundColor Green
    Invoke-MySqlServerStatement "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    Invoke-MySqlDatabaseScript $SchemaFile
    Invoke-MySqlDatabaseScript $DataFile
    Write-Host "Database ready: $DbName" -ForegroundColor Green
}

if (-not $SkipFrontendInstall -and -not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
    Push-Location $FrontendDir
    npm install
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        throw "npm install failed."
    }
    Pop-Location
}

if (-not (Test-Path (Join-Path $Root "uploads"))) {
    New-Item -ItemType Directory -Path (Join-Path $Root "uploads") | Out-Null
}

if (-not (Test-HttpReady $BackendReadyUrl)) {
    Write-Host "Starting backend first..." -ForegroundColor Green
    Start-VisibleProcess $BackendDir "Set-Location '$BackendDir'; .\mvnw.cmd spring-boot:run"

    Write-Host "Waiting for backend to become ready..." -ForegroundColor Yellow
    $BackendReady = $false
    for ($i = 1; $i -le 90; $i++) {
        if (Test-HttpReady $BackendReadyUrl) {
            $BackendReady = $true
            break
        }
        Start-Sleep -Seconds 2
    }
    if (-not $BackendReady) {
        throw "Backend did not become ready within 180 seconds."
    }
} else {
    Write-Host "Backend is already running." -ForegroundColor Yellow
}

if (-not (Test-HttpReady $FrontendUrl)) {
    Write-Host "Starting frontend..." -ForegroundColor Green
    Start-VisibleProcess $FrontendDir "Set-Location '$FrontendDir'; npm run dev"
} else {
    Write-Host "Frontend is already running at $FrontendUrl" -ForegroundColor Yellow
}

Write-Host "Startup sequence complete." -ForegroundColor Cyan
Write-Host "Backend:  http://localhost:8080"
Write-Host "Frontend: $FrontendUrl"
