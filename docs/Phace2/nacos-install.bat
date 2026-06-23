@echo off
echo ========================================
echo   Nacos Server 安装指南
echo ========================================
echo.

echo 步骤 1: 下载 Nacos
echo 访问：https://github.com/alibaba/nacos/releases
echo 下载：nacos-server-1.7.0.zip
echo.

echo 步骤 2: 解压到 D:\env\nacos\
echo.
pause

echo 步骤 3: 启动 Nacos (单机模式)
echo 命令：startup.cmd -m standalone
echo.

echo 是否现在启动 Nacos? (Y/N)
set /p choice=
if /i "%choice%"=="Y" (
    cd /d D:\env\nacos\nacos\bin
    startup.cmd -m standalone
    echo.
    echo Nacos 启动后，访问：http://localhost:8848/nacos
    echo 默认账号：nacos / nacos
) else (
    echo 稍后手动启动
)

echo.
echo ========================================
echo   完成！
echo ========================================
pause
