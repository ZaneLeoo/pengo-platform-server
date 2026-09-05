#requires -Version 7.0

[CmdletBinding()]
param(
    [string]$Version = (Get-Date -Format 'yyyyMMdd-HHmmss'),
    [string]$OutputDirectory = (Join-Path $PSScriptRoot 'output'),
    [string]$MavenCommand = '',
    [string]$JavaHome = '',
    [switch]$SkipBackendTests,
    [switch]$SkipFrontendInstall,
    [string]$BaselineSqlPath = '',
    [switch]$KeepStage
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    Write-Host "`n> $Command $($Arguments -join ' ')" -ForegroundColor Cyan
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "命令执行失败，退出码 $LASTEXITCODE：$Command"
    }
}

function Resolve-MavenCommand {
    if ($MavenCommand) {
        if (-not (Test-Path -LiteralPath $MavenCommand)) {
            throw "指定的 Maven 不存在：$MavenCommand"
        }
        return (Resolve-Path -LiteralPath $MavenCommand).Path
    }

    foreach ($name in @('mvn.cmd', 'mvn')) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            return $command.Source
        }
    }

    $knownMaven = 'C:\App\Dev\Tools\Maven\apache-maven-3.9.16\bin\mvn.cmd'
    if (Test-Path -LiteralPath $knownMaven) {
        return $knownMaven
    }

    throw '未找到 Maven。请安装 Maven，或通过 -MavenCommand 指定 mvn.cmd。'
}

function Resolve-JavaHome {
    if ($JavaHome) {
        $candidate = $JavaHome
    } elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        $candidate = $env:JAVA_HOME
    } else {
        $knownJdk = 'C:\App\Dev\Env\Java\temurin-17\jdk-17.0.20+8'
        if (Test-Path -LiteralPath (Join-Path $knownJdk 'bin\javac.exe')) {
            $candidate = $knownJdk
        } else {
            throw '未找到 JDK 17。请通过 -JavaHome 指定 JDK 17 根目录。'
        }
    }

    $resolved = (Resolve-Path -LiteralPath $candidate).Path
    $versionOutput = & (Join-Path $resolved 'bin\javac.exe') -version 2>&1
    if ($LASTEXITCODE -ne 0 -or "$versionOutput" -notmatch 'javac 17\.') {
        throw "项目必须使用 JDK 17 构建，当前为：$versionOutput"
    }
    return $resolved
}

function Get-GitRevision {
    param([string]$Repository)
    try {
        $revision = & git -c "safe.directory=$Repository" -C $Repository rev-parse HEAD 2>$null
        if ($LASTEXITCODE -eq 0) { return $revision.Trim() }
    } catch {
        # 发布包仍可构建，提交号记为 unknown。
    }
    return 'unknown'
}

function Remove-StageDirectory {
    param([string]$Path, [string]$AllowedParent)
    if (-not (Test-Path -LiteralPath $Path)) { return }

    $resolvedPath = [IO.Path]::GetFullPath($Path).TrimEnd('\')
    $resolvedParent = [IO.Path]::GetFullPath($AllowedParent).TrimEnd('\') + '\'
    if (-not $resolvedPath.StartsWith($resolvedParent, [StringComparison]::OrdinalIgnoreCase)) {
        throw "拒绝删除输出目录之外的路径：$resolvedPath"
    }
    Remove-Item -LiteralPath $resolvedPath -Recurse -Force
}

$safeVersion = $Version -replace '[^0-9A-Za-z._-]', '-'
if (-not $safeVersion) { throw '发布版本不能为空。' }

$backendRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$workspaceRoot = Split-Path $backendRoot -Parent
$frontendRoot = Join-Path $workspaceRoot 'Frontend'
if (-not (Test-Path -LiteralPath (Join-Path $frontendRoot 'package.json'))) {
    throw "未找到前端项目：$frontendRoot"
}

$outputRoot = [IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
$releaseName = "pm-release-$safeVersion"
$stageRoot = Join-Path $outputRoot $releaseName
$packagePath = Join-Path $outputRoot "$releaseName.tar.gz"
$checksumPath = "$packagePath.sha256"

Remove-StageDirectory -Path $stageRoot -AllowedParent $outputRoot
foreach ($path in @(
    (Join-Path $stageRoot 'backend'),
    (Join-Path $stageRoot 'frontend'),
    (Join-Path $stageRoot 'database\migrations'),
    (Join-Path $stageRoot 'deploy\assets'),
    (Join-Path $stageRoot 'deploy\templates'),
    (Join-Path $stageRoot 'deploy\docker')
)) {
    New-Item -ItemType Directory -Force -Path $path | Out-Null
}

$maven = Resolve-MavenCommand
$resolvedJavaHome = Resolve-JavaHome
$originalJavaHome = $env:JAVA_HOME
$originalPath = $env:Path
$env:JAVA_HOME = $resolvedJavaHome
$env:Path = "$(Join-Path $resolvedJavaHome 'bin');$originalPath"
$mavenArguments = @('clean', 'package', '-pl', 'ruoyi-admin', '-am')
if ($SkipBackendTests) { $mavenArguments += '-DskipTests' }
Push-Location $backendRoot
try {
    Invoke-NativeCommand -Command $maven -Arguments $mavenArguments
} finally {
    Pop-Location
    $env:JAVA_HOME = $originalJavaHome
    $env:Path = $originalPath
}

$pnpm = (Get-Command 'pnpm.cmd' -ErrorAction SilentlyContinue)?.Source
if (-not $pnpm) { $pnpm = (Get-Command 'pnpm' -ErrorAction SilentlyContinue)?.Source }
if (-not $pnpm) { throw '未找到 pnpm。请先安装或启用 Corepack。' }

Push-Location $frontendRoot
try {
    if (-not $SkipFrontendInstall) {
        Invoke-NativeCommand -Command $pnpm -Arguments @('install', '--frozen-lockfile')
    }
    Invoke-NativeCommand -Command $pnpm -Arguments @('run', 'build')
} finally {
    Pop-Location
}

$jarPath = Join-Path $backendRoot 'ruoyi-admin\target\ruoyi-admin.jar'
$distPath = Join-Path $frontendRoot 'dist'
if (-not (Test-Path -LiteralPath $jarPath)) { throw "后端产物不存在：$jarPath" }
if (-not (Test-Path -LiteralPath (Join-Path $distPath 'index.html'))) {
    throw "前端产物不存在：$distPath"
}

Copy-Item -LiteralPath $jarPath -Destination (Join-Path $stageRoot 'backend\ruoyi-admin.jar')
Copy-Item -Path (Join-Path $distPath '*') -Destination (Join-Path $stageRoot 'frontend') -Recurse -Force
Copy-Item -Path (Join-Path $PSScriptRoot 'database\migrations\*') -Destination (Join-Path $stageRoot 'database\migrations') -Recurse -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'database\bootstrap_schema_history.sql') -Destination (Join-Path $stageRoot 'database\bootstrap_schema_history.sql')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'deploy.sh') -Destination (Join-Path $stageRoot 'deploy\deploy.sh')
Copy-Item -Path (Join-Path $PSScriptRoot 'assets\*') -Destination (Join-Path $stageRoot 'deploy\assets') -Recurse -Force
Copy-Item -Path (Join-Path $PSScriptRoot 'templates\*') -Destination (Join-Path $stageRoot 'deploy\templates') -Recurse -Force
Copy-Item -Path (Join-Path $PSScriptRoot 'docker\*') -Destination (Join-Path $stageRoot 'deploy\docker') -Recurse -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'README.md') -Destination (Join-Path $stageRoot 'DEPLOYMENT.md')

if ($BaselineSqlPath) {
    $resolvedBaseline = (Resolve-Path -LiteralPath $BaselineSqlPath).Path
    Copy-Item -LiteralPath $resolvedBaseline -Destination (Join-Path $stageRoot 'database\baseline.sql')
    Write-Warning '发布包包含数据库基线，可能含业务数据和账号信息，请通过安全通道传输并及时删除服务器临时文件。'
}

$backendRevision = Get-GitRevision -Repository $backendRoot
$frontendRevision = Get-GitRevision -Repository $frontendRoot
$builtAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss K')

Set-Content -LiteralPath (Join-Path $stageRoot 'RELEASE_ID') -Value $releaseName -Encoding utf8NoBOM
@"
Release: $releaseName
BuiltAt: $builtAt
BackendCommit: $backendRevision
FrontendCommit: $frontendRevision
DatabaseBaseline: 56
IncludesBaselineSql: $([bool]$BaselineSqlPath)
"@ | Set-Content -LiteralPath (Join-Path $stageRoot 'RELEASE.txt') -Encoding utf8NoBOM

if (Test-Path -LiteralPath $packagePath) { Remove-Item -LiteralPath $packagePath -Force }
if (Test-Path -LiteralPath $checksumPath) { Remove-Item -LiteralPath $checksumPath -Force }

$tar = (Get-Command 'tar.exe' -ErrorAction SilentlyContinue)?.Source
if (-not $tar) { $tar = (Get-Command 'tar' -ErrorAction SilentlyContinue)?.Source }
if (-not $tar) { throw '未找到 tar，无法生成 Linux 发布包。' }
Invoke-NativeCommand -Command $tar -Arguments @('-czf', $packagePath, '-C', $outputRoot, $releaseName)

$hash = (Get-FileHash -LiteralPath $packagePath -Algorithm SHA256).Hash.ToLowerInvariant()
"$hash  $releaseName.tar.gz" | Set-Content -LiteralPath $checksumPath -Encoding ascii

if (-not $KeepStage) {
    Remove-StageDirectory -Path $stageRoot -AllowedParent $outputRoot
}

Write-Host "`n发布包构建完成：" -ForegroundColor Green
Write-Host $packagePath
Write-Host "SHA-256：$hash"
Write-Host "上传服务器后执行：sudo /opt/pm/bin/deploy.sh deploy /tmp/$releaseName.tar.gz"
