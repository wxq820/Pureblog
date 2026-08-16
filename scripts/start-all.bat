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
> "%LOGS%\admin.out"   2>nul
> "%LOGS%\admin.err"   2>nul

echo [PureBlog] Starting backend on :8080 ...
start "PureBlogBackend" /B /D "%PROJECT_ROOT%\backend" cmd /c "\"%JAVA_HOME%\bin\java.exe\" -Xms512m -Xmx768m -Dspring.profiles.active=dev -Dlogging.file.path=%LOGS% -jar %PROJECT_ROOT%\backend\pureblog-app\target\pureblog-app-1.0.0.jar > %LOGS%\backend.out 2> %LOGS%\backend.err"

echo [PureBlog] Starting portal on :3000 ...
start "PureBlogPortal" /B /D "%PROJECT_ROOT%\frontend\pureblog-portal" cmd /c "npm.cmd run dev -- --host 127.0.0.1 --port 3000 > %LOGS%\portal.out 2> %LOGS%\portal.err"

echo [PureBlog] Starting admin on :3001 ...
start "PureBlogAdmin" /B /D "%PROJECT_ROOT%\frontend\pureblog-admin-web" cmd /c "npm.cmd run dev -- --host 127.0.0.1 --port 3001 > %LOGS%\admin.out 2> %LOGS%\admin.err"

echo [PureBlog] All three services launched in detached windows.
echo  - backend  : http://127.0.0.1:8080
echo  - portal   : http://127.0.0.1:3000
echo  - admin    : http://127.0.0.1:3001
echo Logs are under %LOGS%
endlocal