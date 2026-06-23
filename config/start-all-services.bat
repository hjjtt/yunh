@echo off
title Start All Services
set "LOG_HOME=d:\vis\yunh\log"
if not exist "%LOG_HOME%\seata" mkdir "%LOG_HOME%\seata"
if not exist "%LOG_HOME%\rocketmq" mkdir "%LOG_HOME%\rocketmq"
if not exist "%LOG_HOME%\sentinel" mkdir "%LOG_HOME%\sentinel"
if not exist "%LOG_HOME%\app" mkdir "%LOG_HOME%\app"
echo ========================================
echo    Start All Config Services
echo ========================================
echo.

echo [1/6] Starting Nacos (standalone)...
start "Nacos Server" cmd /c "cd /d d:\vis\yunh\config\nacos-server-3.1.1\nacos\bin && startup.cmd -m standalone"
timeout /t 3 /nobreak >nul

echo [2/6] Starting Redis...
start "Redis Server" cmd /c "cd /d d:\vis\yunh\config\Redis-x64-5.0.14.1 && redis-server.exe redis.windows.conf"
timeout /t 2 /nobreak >nul

echo [3/6] Starting RocketMQ NameServer...
start "RocketMQ NameServer" cmd /c "d:\vis\yunh\config\start-namesrv.bat"
timeout /t 8 /nobreak >nul

echo [4/6] Starting RocketMQ Broker...
start "RocketMQ Broker" cmd /c "d:\vis\yunh\config\start-broker.bat"
timeout /t 5 /nobreak >nul

echo [5/6] Starting Seata Server...
start "Seata Server" cmd /c "d:\vis\yunh\config\start-seata.bat"
timeout /t 3 /nobreak >nul

echo [6/6] Starting Sentinel Dashboard...
start "Sentinel Dashboard" cmd /c "cd /d d:\vis\yunh\config && java -Dcsp.sentinel.log.dir=d:\vis\yunh\log\sentinel --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -jar sentinel-dashboard-1.7.1.jar --server.port=8098"
timeout /t 2 /nobreak >nul

echo.
echo ========================================
echo    All Services Started!
echo ========================================
echo.
echo    Nacos:       http://localhost:8848/nacos
echo    Redis:       localhost:6379
echo    RocketMQ:    NameServer:9876
echo    Seata:       localhost:8091
echo    Sentinel:    http://localhost:8098
echo.
echo    Press any key to close this window...
pause >nul
