@echo off
REM PureBlog 一键启动脚本（脱离父进程）
setlocal

set PROJECT_ROOT=%~dp0..
set JAVA_HOME=D:\IntelliJ IDEA 2023.3.8\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
set LOGS=%PROJECT_ROOT%\logs

if not exist "%LOGS%" mkdir "%LOGS%"

echo [PureBlog] Truncating logs...
> "%LOGS%\backend.out" 2>nul
> "%LOGS%\backend.err" 2>nul
> "%LOGS%\portal.out"  2>nul
> "%LOGS%\portal.err"  2>nul

echo [PureBlog] Starting backend on :8080 ...
start "PureBlogBackend" /B /D "%PROJECT_ROOT%\backend" cmd /c "\"%JAVA_HOME%\bin\java.exe\" -Xms512m -Xmx768m -Dspring.profiles.active=dev -Dlogging.file.path=%LOGS% -jar %PROJECT_ROOT%\backend\pureblog-app\target\pureblog-app-1.0.0.jar > %LOGS%\backend.out 2> %LOGS%\backend.err"

echo [PureBlog] Starting portal on :3000 ...
start "PureBlogPortal" /B /D "%PROJECT_ROOT%\frontend\pureblog-portal" cmd /c "npm.cmd run dev -- --host 127.0.0.1 --port 3000 > %LOGS%\portal.out 2> %LOGS%\portal.err"

echo [PureBlog] All services launched.
echo  - backend  : http://127.0.0.1:8080
echo  - portal   : http://127.0.0.1:3000  (管理后台: 登录后点击「管理后台」)
echo Logs are under %LOGS%
endlocal
