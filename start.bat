@echo off
echo Starting Rumino...

start "Rumino Backend" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run"
start "Rumino Frontend" cmd /k "cd /d %~dp0frontend && npm start"
