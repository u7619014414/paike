@echo off
REM Jenkins 配置验证脚本
REM 用于验证所有必要的文件和配置是否就绪

echo ========================================
echo Jenkins 多分支流水线配置验证
echo ========================================
echo.

set ERROR_COUNT=0

REM 检查主 Jenkinsfile
echo [1/10] 检查主 Jenkinsfile...
if exist "Jenkinsfile" (
    echo     ✓ Jenkinsfile 存在
) else (
    echo     ✗ Jenkinsfile 不存在
    set /a ERROR_COUNT+=1
)

REM 检查学生系统 Jenkinsfile
echo [2/10] 检查学生系统 Jenkinsfile...
if exist "student-course\Jenkinsfile" (
    echo     ✓ student-course/Jenkinsfile 存在
) else (
    echo     ✗ student-course/Jenkinsfile 不存在
    set /a ERROR_COUNT+=1
)

REM 检查教师系统 Jenkinsfile
echo [3/10] 检查教师系统 Jenkinsfile...
if exist "teacher-scheduling-system\Jenkinsfile" (
    echo     ✓ teacher-scheduling-system/Jenkinsfile 存在
) else (
    echo     ✗ teacher-scheduling-system/Jenkinsfile 不存在
    set /a ERROR_COUNT+=1
)

REM 检查教室管理系统 Dockerfile
echo [4/10] 检查教室管理系统 Dockerfile...
if exist "classroom-timeslot-management\Dockerfile" (
    echo     ✓ classroom-timeslot-management/Dockerfile 存在
) else (
    echo     ✗ classroom-timeslot-management/Dockerfile 不存在
    set /a ERROR_COUNT+=1
)

REM 检查教室管理系统 nginx 配置
echo [5/10] 检查教室管理系统 nginx.conf...
if exist "classroom-timeslot-management\nginx.conf" (
    echo     ✓ classroom-timeslot-management/nginx.conf 存在
) else (
    echo     ✗ classroom-timeslot-management/nginx.conf 不存在
    set /a ERROR_COUNT+=1
)

REM 检查文档
echo [6/10] 检查 Jenkins 配置文档...
if exist "JENKINS.md" (
    echo     ✓ JENKINS.md 存在
) else (
    echo     ✗ JENKINS.md 不存在
    set /a ERROR_COUNT+=1
)

echo [7/10] 检查 Jenkins 设置总结...
if exist "JENKINS_SETUP_SUMMARY.md" (
    echo     ✓ JENKINS_SETUP_SUMMARY.md 存在
) else (
    echo     ✗ JENKINS_SETUP_SUMMARY.md 不存在
    set /a ERROR_COUNT+=1
)

REM 检查 Git 状态
echo [8/10] 检查 Git 仓库状态...
git status >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo     ✓ Git 仓库正常
    echo.
    echo     当前分支:
    git branch --show-current
) else (
    echo     ✗ 不是 Git 仓库或 Git 未安装
    set /a ERROR_COUNT+=1
)

REM 检查 Docker
echo [9/10] 检查 Docker 环境...
docker version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo     ✓ Docker 已安装并运行
) else (
    echo     ✗ Docker 未安装或未运行
    set /a ERROR_COUNT+=1
)

REM 检查 Maven
echo [10/10] 检查 Maven 环境...
mvn -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo     ✓ Maven 已安装
) else (
    echo     ✗ Maven 未安装
    set /a ERROR_COUNT+=1
)

echo.
echo ========================================
echo 验证完成
echo ========================================

if %ERROR_COUNT% EQU 0 (
    echo ✅ 所有检查通过！可以开始配置 Jenkins 流水线。
    echo.
    echo 下一步操作:
    echo 1. 提交并推送代码到 GitHub
    echo 2. 在 Jenkins 中创建多分支流水线
    echo 3. 配置必要的凭据 (github-token, harbor-credentials^)
    echo 4. 触发第一次构建
    echo.
    echo 详细说明请参考: JENKINS.md
) else (
    echo ❌ 发现 %ERROR_COUNT% 个问题，请检查上述错误。
)

echo.
echo 按任意键退出...
pause >nul
