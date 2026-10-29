@echo off
setlocal
set PROJECT_ROOT=%~dp0..
set LOGS=%PROJECT_ROOT%\logs

echo [PureBlog] Stopping backend (:8080) / portal (:3000) ...

REM Backend runs inside the jdk17 container — kill the java process there.
echo   stopping backend in docker (pureblog-jdk17) ...
docker exec pureblog-jdk17 sh -c "pkill -f pureblog-app-1.0.0.jar" >nul 2>&1

REM Only kill the PIDs that actually own the dev ports, so IDE / other java / node are left alone.
for %%P in (3000 8080) do (
    for /f "tokens=5" %%I in ('netstat -ano ^| findstr /R /C:":%%P " ^| findstr "LISTENING"') do (
        if not "%%I"=="0" (
            echo   killing PID %%I on port %%P ...
            taskkill /F /PID %%I >nul 2>&1
        )
    )
)

REM ------------------------------------------------------------
REM 停止基础设施容器
REM 刻意不加 -v: 数据卷必须保留, 否则会连 MySQL 数据一起删掉
REM ------------------------------------------------------------
echo   stopping docker containers (mysql / redis / jdk17) ...
cd /d "%PROJECT_ROOT%"
docker compose down
if errorlevel 1 (
    echo [PureBlog] WARN: docker compose down reported an error (containers may already be stopped).
)

echo [PureBlog] Done.
endlocal
exit /b 0
