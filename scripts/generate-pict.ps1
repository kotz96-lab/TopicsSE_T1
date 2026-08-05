# Regenerate PICT combination tables for every .pict model under pict/models.
# Requires Microsoft PICT — either pict.exe on PATH or dropped at
# tools/pict.exe (see README §5). Downloads: https://github.com/microsoft/pict/releases
#
# Usage:  pwsh scripts/generate-pict.ps1
#
# For each model, produces three files under pict/generated/:
#   <model>-2wise.csv, <model>-3wise.csv, <model>-4wise.csv
#
# PICT natively outputs tab-separated values; we convert to CSV so
# JUnit5's @CsvFileSource can consume the tables without extra config.

$ErrorActionPreference = "Stop"
$root      = Split-Path -Parent $PSScriptRoot
$modelDir  = Join-Path $root "pict\models"
$outDir    = Join-Path $root "pict\generated"
$localExe  = Join-Path $root "tools\pict.exe"

# Resolve which PICT to run: local tools/pict.exe wins, else PATH.
if (Test-Path $localExe) {
    $pict = $localExe
} elseif (Get-Command pict -ErrorAction SilentlyContinue) {
    $pict = "pict"
} else {
    Write-Error "PICT not found. Drop pict.exe at $localExe or install from https://github.com/microsoft/pict/releases"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Get-ChildItem $modelDir -Filter *.pict | ForEach-Object {
    $name = $_.BaseName
    foreach ($strength in 2, 3, 4) {
        $out = Join-Path $outDir "$name-${strength}wise.csv"
        Write-Host "$pict $($_.Name) /o:$strength -> $out"
        # Run PICT (tab-separated output) → convert to CSV.
        # /r:1 pins the random seed so committed CSVs are reproducible.
        $tsv = & $pict $_.FullName "/o:$strength" "/r:1"
        $csv = $tsv -replace "`t", ","
        # ASCII-safe encoding; JUnit5 @CsvFileSource is fine with either.
        $csv | Out-File -Encoding ascii $out
    }
}

Write-Host ""
Write-Host "Row counts:"
Get-ChildItem $outDir -Filter *.csv | Sort-Object Name | ForEach-Object {
    $lines = (Get-Content $_.FullName).Count
    Write-Host ("  {0,-30} {1,5} rows (incl. header)" -f $_.Name, $lines)
}
