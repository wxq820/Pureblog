# PureBlog 一键启动 PowerShell 脚本
# 启动后端、前台 portal、管理后台三个服务，互不依赖
param(
    [switch]$Stop,
    [switch]$Status
)

$ErrorActionPreference = 'SilentlyContinue'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$LogsDir = Join-Path $ProjectRoot 'logs'
$JavaHome = 'D:\IntelliJ IDEA 2023.3.8\jbr'
$JarPath = Join-Path $ProjectRoot 'backend\pureblog-app\target\pureblog-app-1.0.0.jar'
$PortalDir = Join-Path $ProjectRoot 'frontend\pureblog-portal'
$AdminDir = Join-Path $ProjectRoot 'frontend\pureblog-admin-web'

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
    foreach ($port in 3000, 3001, 8080) {
        $conn = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($conn) {
            $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
            Write-Host "  :$port  UP  PID=$($conn.OwningProcess)  $($proc.ProcessName)"
        } else {
            Write-Host "  :$port  DOWN"
        }
    }
    exit 0
}

if (-not (Test-Path $LogsDir)) {
    New-Item -ItemType Directory -Path $LogsDir | Out-Null
}

# 清空日志
'backend', 'portal', 'admin' | ForEach-Object {
    foreach ($ext in 'out', 'err') {
        $p = Join-Path $LogsDir "$_.$ext"
        if (Test-Path $p) { Clear-Content $p -Force }
    }
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

Write-Host "[PureBlog] Starting admin (port 3001) ..."
Start-Detached `
    -FilePath 'cmd.exe' `
    -ArgumentList "/c npm.cmd run dev" `
    -WorkingDirectory $AdminDir `
    -StdoutLog (Join-Path $LogsDir 'admin.out') `
    -StderrLog (Join-Path $LogsDir 'admin.err') `
    -WindowTitle 'PureBlogAdmin'

Write-Host ""
Write-Host "[PureBlog] All launched. Use -Status to check, -Stop to stop."
Write-Host "  - backend  : http://127.0.0.1:8080"
Write-Host "  - portal   : http://127.0.0.1:3000"
Write-Host "  - admin    : http://127.0.0.1:3001"
Write-Host "  - logs     : $LogsDir"