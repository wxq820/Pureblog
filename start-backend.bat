@echo off
set JAVA_HOME=D:\IntelliJ IDEA 2023.3.8\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "C:\Users\14190\Desktop\Pureblog\backend"
java -Xms512m -Xmx768m -Dspring.profiles.active=dev -Dlogging.file.path=C:\Users\14190\Desktop\Pureblog\logs -jar pureblog-app\target\pureblog-app-1.0.0.jar > C:\Users\14190\Desktop\Pureblog\logs\run.out 2> C:\Users\14190\Desktop\Pureblog\logs\run.err