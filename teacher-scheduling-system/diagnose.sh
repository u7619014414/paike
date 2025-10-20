#!/bin/bash
# 诊断脚本：检查 teacher-scheduling-system 部署状态

echo "=========================================="
echo "教师排课系统诊断脚本"
echo "=========================================="
echo ""

cd /data/teacher-scheduling/teacher-scheduling-system

echo "1️⃣ 容器状态："
echo "----------------------------------------"
docker compose ps
echo ""

echo "2️⃣ 后端健康检查："
echo "----------------------------------------"
curl -s http://localhost:45082/api/actuator/health || echo "❌ 后端健康检查失败"
echo ""

echo "3️⃣ 前端健康检查："
echo "----------------------------------------"
curl -s http://localhost:45100/health || echo "❌ 前端健康检查失败"
echo ""

echo "4️⃣ 后端容器日志（最后20行）："
echo "----------------------------------------"
docker compose logs --tail=20 teacher-backend
echo ""

echo "5️⃣ 前端容器日志（最后20行）："
echo "----------------------------------------"
docker compose logs --tail=20 teacher-frontend
echo ""

echo "6️⃣ 网络连接测试："
echo "----------------------------------------"
echo "测试前端容器内访问后端..."
docker exec teacher-scheduling-frontend sh -c "wget -q -O- --timeout=5 http://teacher-backend:45082/api/actuator/health" && echo "✅ 前端可以访问后端" || echo "❌ 前端无法访问后端"
echo ""

echo "7️⃣ Docker 网络信息："
echo "----------------------------------------"
docker network ls | grep teacher
echo ""

echo "8️⃣ 后端容器网络配置："
echo "----------------------------------------"
docker inspect teacher-scheduling-backend --format='{{range .NetworkSettings.Networks}}Network: {{.NetworkID}} IP: {{.IPAddress}}{{end}}'
echo ""

echo "9️⃣ 前端容器网络配置："
echo "----------------------------------------"
docker inspect teacher-scheduling-frontend --format='{{range .NetworkSettings.Networks}}Network: {{.NetworkID}} IP: {{.IPAddress}}{{end}}'
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
