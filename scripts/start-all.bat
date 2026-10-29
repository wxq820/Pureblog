@echo off
REM PureBlog 一键启动 (后端跑在 Docker 里, 前端跑在宿主机)
REM 用法: scripts\start-all.bat
setlocal enabledelayedexpansion

set "PROJECT_ROOT=%~dp0.."
set "LOGS=%PROJECT_ROOT%\logs"
if not exist "%LOGS%" mkdir "%LOGS%"

REM ------------------------------------------------------------
REM 1. 基础设施 (MySQL / Redis / JDK17+Maven)
REM ------------------------------------------------------------
echo [PureBlog] Starting docker infrastructure ...
cd /d "%PROJECT_ROOT%"
docker compose up -d
if errorlevel 1 (
    echo [PureBlog] ERROR: docker compose up failed.
    goto :fail
)

REM 等 MySQL 真正 ready (initdb.d 脚本要跑完)
echo [PureBlog] Waiting for MySQL to be ready ...
set /a _tries=0
:wait_mysql
docker exec pureblog-mysql mysqladmin ping -h localhost -uroot -p123456 >nul 2>&1
if not errorlevel 1 goto :mysql_ok
set /a _tries+=1
if !_tries! geq 60 (
    echo [PureBlog] ERROR: MySQL did not become ready in time.
    goto :fail
)
REM `timeout /t` fails when stdin is redirected; ping -n is a reliable sleep here.
ping -n 3 127.0.0.1 >nul
goto :wait_mysql
:mysql_ok

REM ------------------------------------------------------------
REM 2. 数据库 schema (幂等增量导入)
REM    用 docker cp + 容器内 mysql 导入, 避免 PowerShell/管道把 UTF-8 搞成乱码
REM    initdb.d 只在数据目录为空时执行一次, 所以这里对已存在的库做增量补齐
REM ------------------------------------------------------------
echo [PureBlog] Applying database schema ...
set "SQLLOG=%LOGS%\sql-import.log"
if exist "%SQLLOG%" del "%SQLLOG%"

call :import_sql init_tree.sql
if errorlevel 1 goto :sql_fail
call :import_sql init_widget.sql
if errorlevel 1 goto :sql_fail
call :import_sql init_finance.sql
if errorlevel 1 goto :sql_fail

REM ------------------------------------------------------------
REM 3. 编译后端 (容器内 Maven, 挂载的是宿主机 backend 目录)
REM    不加 clean: 增量构建可省 ~20s
REM ------------------------------------------------------------
echo [PureBlog] Building backend in docker ...
docker exec pureblog-jdk17 sh -c "cd /workspace && mvn -B -DskipTests package" > "%LOGS%\build.out" 2>&1
if errorlevel 1 (
    echo [PureBlog] ERROR: backend build failed, see %LOGS%\build.out
    goto :fail
)

REM ------------------------------------------------------------
REM 4. 启动后端 (容器内 java -jar, 连 mysql/redis 走 service 名)
REM ------------------------------------------------------------
echo [PureBlog] Starting backend in docker (:8080) ...
docker exec pureblog-jdk17 sh -c "pkill -f pureblog-app-1.0.0.jar" >nul 2>&1
docker exec -d pureblog-jdk17 sh -c "cd /workspace && java -jar pureblog-app/target/pureblog-app-1.0.0.jar --spring.profiles.active=dev > /workspace/logs/backend.out 2>&1"

REM 等后端健康检查通过再继续
echo [PureBlog] Waiting for backend health check ...
set /a _tries=0
:wait_health
curl -s -o nul -w "%%{http_code}" http://127.0.0.1:8080/actuator/health 2>nul | findstr "200" >nul
if not errorlevel 1 goto :health_ok
set /a _tries+=1
if !_tries! geq 90 (
    echo [PureBlog] ERROR: backend did not become healthy in time, see %LOGS%\backend.out
    goto :fail
)
ping -n 2 127.0.0.1 >nul
goto :wait_health
:health_ok
echo [PureBlog] Backend is UP.

REM ------------------------------------------------------------
REM 5. 启动前端 (宿主机 npm run dev, /api 反代到 127.0.0.1:8080)
REM ------------------------------------------------------------
echo [PureBlog] Starting portal on :3000 ...
break > "%LOGS%\portal.out" 2>nul
break > "%LOGS%\portal.err" 2>nul
for /f "tokens=5" %%I in ('netstat -ano ^| findstr /R /C:":3000 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%I >nul 2>&1
)
start "PureBlogPortal" /B /D "%PROJECT_ROOT%\frontend\pureblog-portal" cmd /c "npm.cmd run dev -- --host 127.0.0.1 --port 3000 > "%LOGS%\portal.out" 2> "%LOGS%\portal.err""

echo.
echo [PureBlog] All services launched.
echo   - portal  : http://127.0.0.1:3000
echo   - backend : http://127.0.0.1:8080
echo   - admin   : admin / 123456
echo Logs under %LOGS%
endlocal
exit /b 0

REM ------------------------------------------------------------
REM 把一个 SQL 文件导入容器内的 pureblog 库
REM 失败时把错误写进 sql-import.log, 不吞错
REM ------------------------------------------------------------
:import_sql
docker cp "%PROJECT_ROOT%\backend\sql\%~1" pureblog-mysql:/tmp/%~1 >> "%SQLLOG%" 2>&1
if errorlevel 1 (
    echo [PureBlog] ERROR: docker cp %~1 failed
    goto :import_fail
)
docker exec pureblog-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -p123456 pureblog < /tmp/%~1" >> "%SQLLOG%" 2>&1
if errorlevel 1 (
    echo [PureBlog] ERROR: importing %~1 failed
    goto :import_fail
)
exit /b 0
:import_fail
echo [PureBlog] Last 20 lines of %SQLLOG%:
powershell -NoProfile -Command "Get-Content -Tail 20 -Encoding UTF8 '%SQLLOG%'"
exit /b 1

:sql_fail
goto :fail

:fail
echo [PureBlog] Startup FAILED.
endlocal
exit /b 1
