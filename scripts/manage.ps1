# PureBlog 服务管理 PowerShell 脚本
# 用法:
#   .\scripts\manage.ps1          启动后端 + portal
#   .\scripts\manage.ps1 -Status  查看端口状态
#   .\scripts\manage.ps1 -Stop    停止服务
#
# 注: 后端默认跑在 Docker 的 jdk17 容器里 (与 scripts\start-all.bat 一致),
#     本脚本的启动后端分支用于「不跑 Docker、只跑宿主机 JVM」的场景。
param(
    [switch]$Stop,
    [switch]$Status
)

$ErrorActionPreference = 'SilentlyContinue'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$LogsDir = Join-Path $ProjectRoot 'logs'
$JarPath = Join-Path $ProjectRoot 'backend\pureblog-app\target\pureblog-app-1.0.0.jar'
$PortalDir = Join-Path $ProjectRoot 'frontend\pureblog-portal'

function Resolve-JavaHome {
    # 1) 环境变量优先
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        return $env:JAVA_HOME
    }
    # 2) 常见的 JDK 17 安装目录
    $candidates = @(
        'C:\Program Files\Java\jdk-17',
        'C:\Program Files\Java\jdk-21',
        'C:\Program Files\Eclipse Adoptium\jdk-17',
        'C:\Program Files\Eclipse Adoptium\jdk-21'
    )
    foreach ($c in $candidates) {
        if (Test-Path (Join-Path $c 'bin\java.exe')) { return $c }
    }
    # 3) JetBrains 自带的 jbr (取版本号最大的一个)
    $jetbrains = Get-ChildItem 'C:\Program Files\JetBrains' -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'jbr\bin\java.exe') } |
        Sort-Object Name -Descending
    if ($jetbrains) {
        return (Join-Path $jetbrains[0].FullName 'jbr')
    }
    return $null
}

$JavaHome = Resolve-JavaHome

if ($Stop) {
    Write-Host "[PureBlog] Stopping services ..."
    Get-Process | Where-Object { $_.ProcessName -match '^(java|node|npm\.cmd)$' } | ForEach-Object {
        try {
            Write-Host "  kill $($_.ProcessName) PID=$($_.Id)"
            Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
        } catch {}
    }
    Write-Host "[PureBlog] Done."
    exit 0
}

if ($Status) {
    Write-Host "[PureBlog] Service status:"
    # 只有 portal(3000) 与 backend(8080) 两个服务, 没有独立的 admin 前端
    foreach ($port in 3000, 8080) {
        $conn = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($conn) {
            $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
            Write-Host "  :$port  UP  PID=$($conn.OwningProcess)  $($proc.ProcessName)"
        } else {
            Write-Host "  :$port  DOWN"
        }
    }
    Write-Host ""
    Write-Host "  docker containers:"
    docker ps --format "  {{.Names}}`t{{.Status}}" 2>$null
    exit 0
}

if (-not (Test-Path $LogsDir)) {
    New-Item -ItemType Directory -Path $LogsDir | Out-Null
}

# 清空日志
'backend', 'portal' | ForEach-Object {
    foreach ($ext in 'out', 'err') {
        $p = Join-Path $LogsDir "$_.$ext"
        if (Test-Path $p) { Clear-Content $p -Force }
    }
}

if (-not $JavaHome) {
    Write-Host "[PureBlog] ERROR: 未找到 JDK 17+, 请设置 JAVA_HOME 后重试。"
    exit 1
}

function Start-Detached {
    param(
        [string]$FilePath,
        [string]$ArgumentList,
        [string]$WorkingDirectory,
        [string]$StdoutLog,
        [string]$StderrLog,
        [string]$WindowTitle
    )
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $FilePath
    $psi.Arguments = $ArgumentList
    $psi.WorkingDirectory = $WorkingDirectory
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $false
    $psi.RedirectStandardOutput = $false
    $psi.RedirectStandardError = $false

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $psi
    $proc.Start() | Out-Null

    Write-Host "  started  PID=$($proc.Id)  title=$WindowTitle"
}

Write-Host "[PureBlog] Starting backend (port 8080) ..."
Start-Detached `
    -FilePath "$JavaHome\bin\java.exe" `
    -ArgumentList "-Xms512m -Xmx768m -Dspring.profiles.active=dev -Dlogging.file.path=$LogsDir -jar `"$JarPath`"" `
    -WorkingDirectory (Join-Path $ProjectRoot 'backend') `
    -StdoutLog (Join-Path $LogsDir 'backend.out') `
    -StderrLog (Join-Path $LogsDir 'backend.err') `
    -WindowTitle 'PureBlogBackend'

Write-Host "[PureBlog] Starting portal (port 3000) ..."
Start-Detached `
    -FilePath 'cmd.exe' `
    -ArgumentList "/c npm.cmd run dev" `
    -WorkingDirectory $PortalDir `
    -StdoutLog (Join-Path $LogsDir 'portal.out') `
    -StderrLog (Join-Path $LogsDir 'portal.err') `
    -WindowTitle 'PureBlogPortal'

Write-Host ""
Write-Host "[PureBlog] All launched. Use -Status to check, -Stop to stop."
Write-Host "  - backend  : http://127.0.0.1:8080"
Write-Host "  - portal   : http://127.0.0.1:3000"
Write-Host "  - logs     : $LogsDir"
