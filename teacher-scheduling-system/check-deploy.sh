#!/bin/bash
# 快速诊断部署问题

cd /data/1vueproj/paike/teacher-scheduling-system

echo "=========================================="
echo "1. 检查代码版本"
echo "=========================================="
git log --oneline -3
echo ""
git branch -v
echo ""

echo "=========================================="
echo "2. 检查 application-prod.yml 是否存在"
echo "=========================================="
if [ -f "backend/src/main/resources/application-prod.yml" ]; then
    echo "✅ application-prod.yml 存在"
    echo "内容预览:"
    head -20 backend/src/main/resources/application-prod.yml
else
    echo "❌ application-prod.yml 不存在！"
fi
echo ""

echo "=========================================="
echo "3. 检查 docker-compose.yml 配置"
echo "=========================================="
cat docker-compose.yml | grep -A 3 "SPRING_PROFILES_ACTIVE"
echo ""

echo "=========================================="
echo "4. 容器状态"
echo "=========================================="
docker compose ps
echo ""

echo "=========================================="
echo "5. 后端日志（最后 50 行）"
echo "=========================================="
docker compose logs --tail=50 teacher-backend
echo ""

echo "=========================================="
echo "6. 网络信息"
echo "=========================================="
docker network ls | grep teacher
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
