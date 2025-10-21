#!/bin/bash
# Jenkins 配置验证脚本
# 用于验证所有必要的文件和配置是否就绪

echo "========================================"
echo "Jenkins 多分支流水线配置验证"
echo "========================================"
echo ""

ERROR_COUNT=0

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查主 Jenkinsfile
echo "[1/10] 检查主 Jenkinsfile..."
if [ -f "Jenkinsfile" ]; then
    echo -e "    ${GREEN}✓${NC} Jenkinsfile 存在"
else
    echo -e "    ${RED}✗${NC} Jenkinsfile 不存在"
    ((ERROR_COUNT++))
fi

# 检查学生系统 Jenkinsfile
echo "[2/10] 检查学生系统 Jenkinsfile..."
if [ -f "student-course/Jenkinsfile" ]; then
    echo -e "    ${GREEN}✓${NC} student-course/Jenkinsfile 存在"
else
    echo -e "    ${RED}✗${NC} student-course/Jenkinsfile 不存在"
    ((ERROR_COUNT++))
fi

# 检查教师系统 Jenkinsfile
echo "[3/10] 检查教师系统 Jenkinsfile..."
if [ -f "teacher-scheduling-system/Jenkinsfile" ]; then
    echo -e "    ${GREEN}✓${NC} teacher-scheduling-system/Jenkinsfile 存在"
else
    echo -e "    ${RED}✗${NC} teacher-scheduling-system/Jenkinsfile 不存在"
    ((ERROR_COUNT++))
fi

# 检查教室管理系统 Dockerfile
echo "[4/10] 检查教室管理系统 Dockerfile..."
if [ -f "classroom-timeslot-management/Dockerfile" ]; then
    echo -e "    ${GREEN}✓${NC} classroom-timeslot-management/Dockerfile 存在"
else
    echo -e "    ${RED}✗${NC} classroom-timeslot-management/Dockerfile 不存在"
    ((ERROR_COUNT++))
fi

# 检查教室管理系统 nginx 配置
echo "[5/10] 检查教室管理系统 nginx.conf..."
if [ -f "classroom-timeslot-management/nginx.conf" ]; then
    echo -e "    ${GREEN}✓${NC} classroom-timeslot-management/nginx.conf 存在"
else
    echo -e "    ${RED}✗${NC} classroom-timeslot-management/nginx.conf 不存在"
    ((ERROR_COUNT++))
fi

# 检查文档
echo "[6/10] 检查 Jenkins 配置文档..."
if [ -f "JENKINS.md" ]; then
    echo -e "    ${GREEN}✓${NC} JENKINS.md 存在"
else
    echo -e "    ${RED}✗${NC} JENKINS.md 不存在"
    ((ERROR_COUNT++))
fi

echo "[7/10] 检查 Jenkins 设置总结..."
if [ -f "JENKINS_SETUP_SUMMARY.md" ]; then
    echo -e "    ${GREEN}✓${NC} JENKINS_SETUP_SUMMARY.md 存在"
else
    echo -e "    ${RED}✗${NC} JENKINS_SETUP_SUMMARY.md 不存在"
    ((ERROR_COUNT++))
fi

# 检查 Git 状态
echo "[8/10] 检查 Git 仓库状态..."
if git status &>/dev/null; then
    echo -e "    ${GREEN}✓${NC} Git 仓库正常"
    echo ""
    echo "    当前分支: $(git branch --show-current)"
else
    echo -e "    ${RED}✗${NC} 不是 Git 仓库或 Git 未安装"
    ((ERROR_COUNT++))
fi

# 检查 Docker
echo "[9/10] 检查 Docker 环境..."
if docker version &>/dev/null; then
    echo -e "    ${GREEN}✓${NC} Docker 已安装并运行"
else
    echo -e "    ${RED}✗${NC} Docker 未安装或未运行"
    ((ERROR_COUNT++))
fi

# 检查 Maven
echo "[10/10] 检查 Maven 环境..."
if mvn -version &>/dev/null; then
    echo -e "    ${GREEN}✓${NC} Maven 已安装"
else
    echo -e "    ${RED}✗${NC} Maven 未安装"
    ((ERROR_COUNT++))
fi

echo ""
echo "========================================"
echo "验证完成"
echo "========================================"

if [ $ERROR_COUNT -eq 0 ]; then
    echo -e "${GREEN}✅ 所有检查通过！可以开始配置 Jenkins 流水线。${NC}"
    echo ""
    echo "下一步操作:"
    echo "1. 提交并推送代码到 GitHub"
    echo "2. 在 Jenkins 中创建多分支流水线"
    echo "3. 配置必要的凭据 (github-token, harbor-credentials)"
    echo "4. 触发第一次构建"
    echo ""
    echo "详细说明请参考: JENKINS.md"
else
    echo -e "${RED}❌ 发现 $ERROR_COUNT 个问题，请检查上述错误。${NC}"
fi

echo ""
