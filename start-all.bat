@echo off
chcp 65001 >nul
echo ========================================
echo 启动排课系统所有服务
echo ========================================
echo.

echo [1/4] 启动学生选课系统后端 (端口: 45081)
start "学生选课后端" cmd /k "cd student-course-backend && mvn spring-boot:run"
timeout /t 3 >nul

echo [2/4] 启动教师排课系统后端 (端口: 45082)
start "教师排课后端" cmd /k "cd teacher-scheduling-system\backend && mvn spring-boot:run"
timeout /t 3 >nul

echo [3/4] 启动学生选课系统前端 (端口: 3000)
start "学生选课前端" cmd /k "cd student-course-frontend && pnpm dev"
timeout /t 2 >nul

echo [4/4] 启动教师排课系统前端 (端口: 45100)
start "教师排课前端" cmd /k "cd teacher-scheduling-system\frontend && pnpm dev"

echo.
echo ========================================
echo 所有服务启动中，请稍候...
echo ========================================
echo.
echo 服务地址：
echo - 学生选课后端: http://localhost:45081
echo - 学生选课前端: http://localhost:3000
echo - 教师排课后端: http://localhost:45082/api
echo - 教师排课前端: http://localhost:45100
echo ========================================
pause
