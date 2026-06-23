@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-20"
set "ROCKETMQ_HOME=d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
cd /d "%ROCKETMQ_HOME%\bin"
call mqnamesrv.cmd
