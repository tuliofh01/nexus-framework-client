#Requires -Version 5.1
<#
.SYNOPSIS
  Windows test-gen entry point (PowerShell). Prefer WSL for Nexus client builds.
.PARAMETER Project
  Project name under builds/framework/<name>.
.PARAMETER Path
  Direct path to generated app root.
.PARAMETER DryRun
  Print planned writes without creating files.
.PARAMETER Force
  Overwrite files that contain the nexus-test-gen marker.
#>
[CmdletBinding()]
param(
    [string]$Project,
    [string]$Path,
    [switch]$DryRun,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$GenerateSh = Join-Path $ScriptDir 'generate-tests.sh'

if (-not (Get-Command bash -ErrorAction SilentlyContinue)) {
    throw 'bash is required (Git Bash or WSL). Install Git for Windows or enable WSL.'
}

$argsList = @()
if ($DryRun) { $argsList += '--dry-run' }
if ($Force) { $argsList += '--force' }
if ($Project) {
    $argsList += '--project'
    $argsList += $Project
} elseif ($Path) {
    $argsList += $Path
} else {
    throw 'Specify -Project <name> or -Path <dir>'
}

Write-Host 'test-gen (win32): optional gtest via vcpkg or Visual Studio Test Adapter'
& bash $GenerateSh @argsList
