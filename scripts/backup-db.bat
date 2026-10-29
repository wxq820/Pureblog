@echo off
REM PureBlog 数据库备份
REM 用法: scripts\backup-db.bat
REM 生成: backups\pureblog-YYYYMMDD-HHMM.sql, 只保留最近 14 份
setlocal

set "PROJECT_ROOT=%~dp0.."
set "BACKUP_DIR=%PROJECT_ROOT%\backups"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM 生成时间戳 YYYYMMDD-HHMM
set "STAMP=%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%"
if "%time:~0,2%"=="0 " set "STAMP=%date:~0,4%%date:~5,2%%date:~8,2%-0%time:~1,1%%time:~3,2%"

set "TARGET=%BACKUP_DIR%\pureblog-%STAMP%.sql"

echo [PureBlog] Dumping database to %TARGET% ...
docker exec pureblog-mysql mysqldump -uroot -p123456 ^
  --single-transaction --routines --triggers --default-character-set=utf8mb4 ^
  pureblog > "%TARGET%"

if errorlevel 1 (
    echo [PureBlog] ERROR: mysqldump failed.
    if exist "%TARGET%" del "%TARGET%"
    endlocal
    exit /b 1
)

REM 空文件说明 dump 实际失败 (stderr 被重定向进文件, 退出码可能仍为 0)
for %%F in ("%TARGET%") do set "SIZE=%%~zF"
if "%SIZE%"=="0" (
    echo [PureBlog] ERROR: dump produced an empty file.
    del "%TARGET%"
    endlocal
    exit /b 1
)

echo [PureBlog] Backup OK: %TARGET% (%SIZE% bytes)

REM 保留最近 14 份, 其余删除
for /f "skip=14 delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\pureblog-*.sql"') do del "%BACKUP_DIR%\%%f"

endlocal
exit /b 0
