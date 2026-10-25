@echo off
title IoT Home Automation Server
echo ====================================================================
echo Starting IoT Home Automation System (Spring Boot 3)
echo Open your browser at: http://localhost:8080/index.html
echo Default Credentials : admin / Admin@Home2026!
echo ====================================================================
cd /d "%~dp0backend"
if exist "target\iot-home-automation-1.0.0.jar" (
    java -jar target\iot-home-automation-1.0.0.jar %*
) else (
    echo Building application with Maven...
    call mvn clean package -DskipTests
    java -jar target\iot-home-automation-1.0.0.jar %*
)
pause
