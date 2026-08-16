@echo off
setlocal
set PROJECT_ROOT=%~dp0..
set LOGS=%PROJECT_ROOT%\logs

echo [PureBlog] Stopping backend / portal / admin ...
for /f "tokens=*" %%T in ('tasklist /FI "WINDOWTITLE eq PureBlogBackend*" /NH /FO CSV 2^>nul ^| findstr /I "PureBlogBackend"') do (
    for /f "tokens=2 delims=," %%P in ("%%T") do taskkill /F /PID %%P >nul 2>&1
)
for /f "tokens=*" %%T in ('tasklist /FI "WINDOWTITLE eq PureBlogPortal*" /NH /FO CSV 2^>nul ^| findstr /I "PureBlogPortal"') do (
    for /f "tokens=2 delims=," %%P in ("%%T") do taskkill /F /PID %%P >nul 2>&1
)
for /f "tokens=*" %%T in ('tasklist /FI "WINDOWTITLE eq PureBlogAdmin*" /NH /FO CSV 2^>nul ^| findstr /I "PureBlogAdmin"') do (
    for /f "tokens=2 delims=," %%P in ("%%T") do taskkill /F /PID %%P >nul 2>&1
)

taskkill /F /IM java.exe    >nul 2>&1
taskkill /F /IM node.exe    >nul 2>&1
taskkill /F /IM npm.cmd     >nul 2>&1

echo [PureBlog] Done.
endlocal