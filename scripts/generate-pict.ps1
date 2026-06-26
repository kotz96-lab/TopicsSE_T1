# Regenerate PICT combination tables for every .pict model under pict/models.
# Requires Microsoft PICT on PATH (https://github.com/microsoft/pict).
# Usage:  pwsh scripts/generate-pict.ps1
#
# Output goes under pict/generated/<model>-{2,3,4}wise.txt.

$ErrorActionPreference = "Stop"
$root      = Split-Path -Parent $PSScriptRoot
$modelDir  = Join-Path $root "pict\models"
$outDir    = Join-Path $root "pict\generated"

if (-not (Get-Command pict -ErrorAction SilentlyContinue)) {
    Write-Error "pict not found on PATH. Install from https://github.com/microsoft/pict"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Get-ChildItem $modelDir -Filter *.pict | ForEach-Object {
    $name = $_.BaseName
    foreach ($strength in 2, 3, 4) {
        $out = Join-Path $outDir "$name-${strength}wise.txt"
        Write-Host "pict $($_.FullName) /o:$strength -> $out"
        pict $_.FullName "/o:$strength" | Out-File -Encoding ascii $out
    }
}
