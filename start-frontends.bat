@echo off
chcp 65001 >nul
echo ========================================
echo 启动前端服务
echo ========================================
echo.

echo [1/2] 启动学生选课系统前端 (端口: 3000)
start "学生选课前端-3000" cmd /k "cd student-course-frontend && pnpm dev"
timeout /t 2 >nul

echo [2/2] 启动教师排课系统前端 (端口: 45100)
start "教师排课前端-45100" cmd /k "cd teacher-scheduling-system\frontend && pnpm dev"

echo.
echo ========================================
echo 前端服务启动中，请等待约5-10秒...
echo ========================================
echo.
echo 访问地址：
echo - 学生选课系统: http://localhost:3000
echo - 教师排课系统: http://localhost:45100
echo.
echo 注意：请确保后端服务已启动
echo ========================================
pause
