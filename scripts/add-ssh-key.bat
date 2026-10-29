@echo off
REM ============================================================
REM 一次性操作: 把 GitHub 私钥加载进 ssh-agent
REM 运行方式: 双击本文件, 或在终端执行 scripts\add-ssh-key.bat
REM
REM 过程中会提示输入 passphrase (你设置私钥时的那串口令),
REM 输入时终端不显示任何字符, 属正常现象, 直接敲回车即可。
REM ============================================================

echo ============================================================
echo   PureBlog - 加载 GitHub SSH 私钥到 ssh-agent
echo ============================================================
echo.

where ssh-add >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 ssh-add, 请确认已安装 OpenSSH Client。
    pause
    exit /b 1
)

REM 确保 agent 服务已启动
sc query ssh-agent | find "RUNNING" >nul
if errorlevel 1 (
    echo [提示] ssh-agent 未运行, 正在尝试启动...
    net start ssh-agent >nul 2>&1
)

REM 优先使用 github 专用私钥, 回退到 id_ed25519
set KEYFILE=%USERPROFILE%\.ssh\github_ed25519
if not exist "%KEYFILE%" set KEYFILE=%USERPROFILE%\.ssh\id_ed25519

if not exist "%KEYFILE%" (
    echo [错误] 未找到私钥文件, 请检查 %USERPROFILE%\.ssh 目录。
    pause
    exit /b 1
)

echo 正在加载: %KEYFILE%
echo (如提示 Enter passphrase, 直接输入你设置私钥时的口令并回车)
echo.

ssh-add "%KEYFILE%"
if errorlevel 1 (
    echo.
    echo [失败] 私钥加载失败, 请检查 passphrase 是否正确。
    pause
    exit /b 1
)

echo.
echo [成功] 当前 agent 中的身份:
ssh-add -l
echo.
echo 可以测试连接: ssh -T git@ssh.github.com
echo 注意: ssh.github.com 是 GitHub 官方的 443 端口通道, 不受 22 端口封锁影响。
echo.
pause
