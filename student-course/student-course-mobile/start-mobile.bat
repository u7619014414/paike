@echo off
echo ========================================
echo 启动学生选课系统 - 移动端
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] 检查依赖...
if not exist "node_modules\" (
    echo 未找到依赖,正在安装...
    call pnpm install
    if errorlevel 1 (
        echo 依赖安装失败!
        pause
        exit /b 1
    )
) else (
    echo 依赖已存在
)

echo.
echo [2/2] 启动开发服务器...
echo 服务器将在 http://localhost:3001 启动
echo 按 Ctrl+C 停止服务器
echo.

call pnpm dev

pause
