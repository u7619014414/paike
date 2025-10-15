@echo off
chcp 65001 >nul
echo ====================================
echo 学生选课系统 - 初始化测试数据
echo ====================================
echo.
echo 此脚本将导入测试数据到school_system数据库
echo 包括：时间段、教室、课程、课程安排、测试学生
echo.
echo 注意：这将清空现有的学生选课数据！
echo.
pause

echo.
echo 正在导入数据...
echo.

REM 使用MySQL命令导入数据
mysql -h asdnn.com -P 45306 -u school_user -pschool_pass123 school_system < init-student-data.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo 数据导入成功！
    echo ====================================
    echo.
    echo 您现在可以登录学生选课系统进行测试
    echo 测试学生账号：STU20250001 到 STU20250018
    echo.
) else (
    echo.
    echo ====================================
    echo 数据导入失败！
    echo ====================================
    echo.
    echo 请检查：
    echo 1. MySQL是否安装并在PATH中
    echo 2. 数据库连接信息是否正确
    echo 3. 数据库schema是否已创建
    echo.
)

pause
