@echo off
setlocal

set PROJECT_ROOT=D:\HuaweiMoveData\Users\Ustinian\Desktop\Pureblog-temp
set JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
set LOGS=%PROJECT_ROOT%\logs

if not exist "%LOGS%" mkdir "%LOGS%"

> "%LOGS%\backend.out" 2>nul
> "%LOGS%\backend.err" 2>nul

echo [PureBlog] Starting backend on :8080 ...
start "PureBlogBackend" /B /D "%PROJECT_ROOT%\backend" cmd /c "\"%JAVA_HOME%\bin\java.exe\" -Xms512m -Xmx768m -Dspring.profiles.active=dev -Dspring.config.location=file:%PROJECT_ROOT%\backend\pureblog-app\src\main\resources\application-dev.yml -Dlogging.file.path=%LOGS% -jar %PROJECT_ROOT%\backend\pureblog-app\target\pureblog-app-1.0.0.jar > %LOGS%\backend.out 2> %LOGS%\backend.err"

echo [PureBlog] Backend launched.
endlocal