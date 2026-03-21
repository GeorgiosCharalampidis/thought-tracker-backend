@echo off
echo Starting MindLog...

start "MindLog Backend" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run"
start "MindLog Frontend" cmd /k "cd /d %~dp0frontend && npm start"
