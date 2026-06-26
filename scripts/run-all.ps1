# End-to-end pipeline for Person A's deliverables.
# Builds, runs JUnit5 suite, gathers JaCoCo coverage, runs PIT mutation testing.
# Output reports go to target/site/jacoco/ and target/pit-reports/.
#
# Usage:  pwsh scripts/run-all.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
    Write-Host "==> ./mvnw clean verify  (compile + JUnit5 + JaCoCo)" -ForegroundColor Cyan
    & .\mvnw.cmd clean verify
    if ($LASTEXITCODE -ne 0) { throw "mvnw verify failed with $LASTEXITCODE" }

    Write-Host "==> ./mvnw -Ppit test  (PIT mutation coverage)" -ForegroundColor Cyan
    & .\mvnw.cmd -Ppit test
    if ($LASTEXITCODE -ne 0) { throw "mvnw -Ppit test failed with $LASTEXITCODE" }

    Write-Host ""
    Write-Host "Reports:" -ForegroundColor Green
    Write-Host "  Coverage: target/site/jacoco/index.html"
    Write-Host "  Mutation: target/pit-reports/index.html"
}
finally {
    Pop-Location
}
