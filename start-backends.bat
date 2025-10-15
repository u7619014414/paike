@echo off
chcp 65001 >nul
echo ========================================
echo 启动后端服务
echo ========================================
echo.

echo [1/2] 启动学生选课系统后端 (端口: 45081)
start "学生选课后端-45081" cmd /k "cd student-course-backend && mvn spring-boot:run"
timeout /t 3 >nul

echo [2/2] 启动教师排课系统后端 (端口: 45082)
start "教师排课后端-45082" cmd /k "cd teacher-scheduling-system\backend && mvn spring-boot:run"

echo.
echo ========================================
echo 后端服务启动中，请等待约10-15秒...
echo ========================================
echo.
echo 服务地址：
echo - 学生选课后端: http://localhost:45081
echo - 教师排课后端: http://localhost:45082/api
echo.
echo 提示：后端服务启动完成后，可以双击 start-frontends.bat 启动前端
echo ========================================
pause
