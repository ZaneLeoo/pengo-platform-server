#requires -Version 7.0

<#
.SYNOPSIS
    项目管理系统本地一键发布脚本。

.DESCRIPTION
    按顺序完成：构建发布包、生成校验值、SCP 上传、安装服务器 pmctl.sh、
    调用服务器部署、最后从公网执行健康检查。

    脚本只支持 SSH 密钥认证，不接收或保存服务器密码。
    日常发布不要传 BaselineSqlPath；数据库变化应放入 database/migrations/V*.sql。
#>

[CmdletBinding()]
param(
    [string]$Version = (Get-Date -Format 'yyyyMMdd-HHmmss'),
    [string]$Server = '42.194.137.17',
    [string]$SshUser = 'root',
    [ValidateRange(1, 65535)][int]$SshPort = 22,
    [Parameter(Mandatory = $true)][string]$IdentityFile,
    [string]$DeployRoot = '/www/wwwroot/pm',
    [string]$PublicBaseUrl = 'http://42.194.137.17:9000',
    [string]$MavenCommand = 'C:\App\Dev\Tools\Maven\apache-maven-3.9.16\bin\mvn.cmd',
    [string]$JavaHome = 'C:\App\Dev\Env\Java\temurin-17\jdk-17.0.20+8',
    [switch]$SkipBackendTests,
    [switch]$SkipFrontendInstall,
    [string]$BaselineSqlPath = ''
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-NativeChecked {
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

# 限制远程路径字符，避免路径被 shell 当成额外命令解析。
if ($DeployRoot -notmatch '^/[0-9A-Za-z._/-]+$' -or $DeployRoot.Contains('..')) {
    throw "部署目录不安全：$DeployRoot"
}

$identity = (Resolve-Path -LiteralPath $IdentityFile).Path
$buildScript = Join-Path $PSScriptRoot 'build-release.ps1'
$safeVersion = $Version -replace '[^0-9A-Za-z._-]', '-'
$releaseName = "pm-release-$safeVersion"
$package = Join-Path $PSScriptRoot "output\$releaseName.tar.gz"
$checksum = "$package.sha256"

# 1. 在本机完成 Maven/Vite 构建和打包。
$buildArguments = @(
    '-NoProfile', '-File', $buildScript,
    '-Version', $Version,
    '-MavenCommand', $MavenCommand,
    '-JavaHome', $JavaHome
)
if ($SkipBackendTests) { $buildArguments += '-SkipBackendTests' }
if ($SkipFrontendInstall) { $buildArguments += '-SkipFrontendInstall' }
if ($BaselineSqlPath) { $buildArguments += @('-BaselineSqlPath', $BaselineSqlPath) }
Invoke-NativeChecked -Command 'pwsh' -Arguments $buildArguments

if (-not (Test-Path -LiteralPath $package) -or -not (Test-Path -LiteralPath $checksum)) {
    throw "构建结束但未找到发布包：$package"
}

$target = "${SshUser}@${Server}"
$commonSsh = @(
    '-i', $identity,
    '-p', "$SshPort",
    '-o', 'BatchMode=yes',
    '-o', 'StrictHostKeyChecking=accept-new'
)

# 2. 上传发布包和 SHA-256 文件。SCP 的端口参数为大写 -P。
$scpArguments = @(
    '-i', $identity,
    '-P', "$SshPort",
    '-o', 'BatchMode=yes',
    '-o', 'StrictHostKeyChecking=accept-new',
    $package,
    $checksum,
    "${target}:${DeployRoot}/"
)
Invoke-NativeChecked -Command 'scp' -Arguments $scpArguments

# 3. 从发布包提取最新版 pmctl.sh，再由 pmctl 完成备份、迁移和版本切换。
$remotePackage = "$DeployRoot/$releaseName.tar.gz"
$remoteCommand = "set -Eeuo pipefail; cd '$DeployRoot'; " +
    "sha256sum -c '$releaseName.tar.gz.sha256'; " +
    "tar -xOf '$remotePackage' '$releaseName/deploy/docker/pmctl.sh' > pmctl.sh.new; " +
    # Windows 工作区可能保留 CRLF；部署前统一为 Linux 换行并做 Bash 语法检查。
    "sed -i 's/\r$//' pmctl.sh.new; bash -n pmctl.sh.new; " +
    "chmod 700 pmctl.sh.new; mv -f pmctl.sh.new pmctl.sh; " +
    "bash ./pmctl.sh deploy '$remotePackage'"
$sshArguments = $commonSsh + @($target, $remoteCommand)
Invoke-NativeChecked -Command 'ssh' -Arguments $sshArguments

# 4. 从开发机再次验证公网入口，而不仅依赖服务器本机 curl。
$healthUrl = $PublicBaseUrl.TrimEnd('/') + '/dev-api/health'
$health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 20
if ($health.status -ne 'UP') {
    throw "公网健康检查失败：$($health | ConvertTo-Json -Compress)"
}

Write-Host "`n发布成功：$releaseName" -ForegroundColor Green
Write-Host "访问地址：$($PublicBaseUrl.TrimEnd('/'))"
