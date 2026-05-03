@echo off
setlocal
set MVNW_DIR=%~dp0
set MAVEN_VERSION=3.9.9
set MAVEN_HOME=%MVNW_DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%
set MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd

if not exist "%MAVEN_BIN%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$root='%MVNW_DIR%.mvn\wrapper';" ^
    "$version='%MAVEN_VERSION%';" ^
    "$zip=Join-Path $root ('apache-maven-' + $version + '-bin.zip');" ^
    "$url='https://archive.apache.org/dist/maven/maven-3/' + $version + '/binaries/apache-maven-' + $version + '-bin.zip';" ^
    "New-Item -ItemType Directory -Force -Path $root | Out-Null;" ^
    "if (!(Test-Path $zip)) { Invoke-WebRequest -Uri $url -OutFile $zip };" ^
    "Expand-Archive -Path $zip -DestinationPath $root -Force"
)

"%MAVEN_BIN%" %*
