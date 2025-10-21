# 学生选课移动端配置文档

## 📋 概述

本文档说明了学生选课移动端（student-course-mobile）的 Docker 和 Jenkins CI/CD 配置。

## 🎯 端口分配

所有端口统一在 **45080-45090** 区间内：

| 服务 | 端口 | 说明 |
|------|------|------|
| 学生后端 | 45081 | Spring Boot API 服务 |
| 学生PC前端 | 45083 | Vue 3 桌面端 |
| 学生移动端 | 45084 | Vue 3 + Vant 移动端 |

## 📦 已创建文件

### 1. Docker 相关文件

```
student-course-mobile/
├── Dockerfile              # 多阶段构建配置
├── nginx.conf              # Nginx 配置（含 API 代理）
└── .dockerignore           # Docker 忽略文件
```

#### Dockerfile 说明
- **构建阶段**: Node 18 Alpine + pnpm
- **运行阶段**: Nginx 1.25 Alpine
- **特性**:
  - 多阶段构建减少镜像体积
  - 健康检查端点：`/health`
  - 暴露端口：80（容器内）

#### nginx.conf 特性
- Vue Router History 模式支持
- Gzip 压缩
- 静态资源缓存（1年）
- API 代理到后端服务
- 移动端优化（禁用缓存）
- 安全头配置

### 2. Docker Compose 配置

在 `student-course/docker-compose.yml` 中添加了 mobile 服务：

```yaml
student-mobile:
  image: u7619014414/shibx:student-mobile-latest
  container_name: student-course-mobile
  ports:
    - "45084:80"
  networks:
    - student-course-network
  depends_on:
    - student-backend
  healthcheck:
    test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost/health"]
    interval: 30s
    timeout: 3s
    retries: 3
  restart: unless-stopped
```

### 3. Jenkins 流水线配置

#### student-course/Jenkinsfile

新增**第4阶段：构建移动端**

```groovy
stage('4️⃣ 构建移动端') {
    steps {
        echo "==== [4] 移动端前端构建（直接使用 Docker） ===="
        bat """
            cd /d "${LOCAL_CODE_DIR}\\student-course\\student-course-mobile"
            docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} .
            docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:latest
        """
    }
}
```

**推送镜像**：在第5阶段添加移动端镜像推送

**部署脚本**：
- 检查移动端日志
- 健康检查：`http://localhost:45084/health`
- 显示移动端访问地址

#### paike/Jenkinsfile（主流水线）

在**学生选课系统并行构建**中新增：

```groovy
stage('学生移动端构建') {
    steps {
        echo "==== 构建学生选课移动端 ===="
        bat """
            cd /d "${LOCAL_CODE_DIR}\\student-course\\student-course-mobile"
            docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} .
            docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:latest
            docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${GIT_BRANCH}
        """
    }
}
```

**推送镜像**：添加移动端镜像到学生系统镜像推送列表

**docker-compose.override.yml**：添加 student-mobile 服务配置

## 🚀 使用方法

### 本地开发

```bash
# 进入移动端目录
cd student-course/student-course-mobile

# 安装依赖
pnpm install

# 启动开发服务器（端口 3001）
pnpm dev

# 构建生产版本
pnpm build
```

### Docker 构建

```bash
# 构建镜像
docker build -t student-course-mobile:latest .

# 运行容器
docker run -d -p 45084:80 student-course-mobile:latest
```

### Docker Compose 部署

```bash
# 进入 student-course 目录
cd student-course

# 启动所有服务
docker compose up -d

# 仅启动移动端
docker compose up -d student-mobile

# 查看日志
docker compose logs -f student-mobile

# 停止服务
docker compose down
```

### Jenkins 构建

#### 方式 1：使用 student-course 独立流水线

1. 触发构建：`student-course/Jenkinsfile`
2. 构建所有三个服务：后端 + PC前端 + 移动端
3. 自动推送到 Harbor
4. 自动部署到服务器

#### 方式 2：使用主流水线

1. 触发构建：`paike/Jenkinsfile`
2. 选择参数：
   - `BUILD_SCOPE`: `STUDENT_COURSE` 或 `ALL`
   - 其他参数按需选择
3. 自动构建、推送、部署

## 📊 Harbor 镜像

镜像命名：`asdnn.com:45443/yl/student-course-mobile:TAG`

标签策略：
- `构建号`（如：123）
- `latest`
- `分支名`（如：develop、master）

## 🔍 验证部署

### 检查容器状态

```bash
# SSH 到服务器
ssh -i D:\1code\powershell\jenkins_id_rsa -p 45000 root@asdnn.com

# 进入部署目录
cd /data/1vueproj/paike/student-course

# 查看容器
docker compose ps

# 查看移动端日志
docker compose logs --tail=50 student-mobile

# 检查健康状态
curl http://localhost:45084/health
```

### 访问应用

- **移动端**: http://asdnn.com:45084
- **PC前端**: http://asdnn.com:45083
- **后端 API**: http://asdnn.com:45081/api

### 健康检查

```bash
# 移动端健康检查
curl http://asdnn.com:45084/health

# 后端健康检查
curl http://asdnn.com:45081/actuator/health
```

## 🛠️ 技术栈

### 前端
- **框架**: Vue 3 + TypeScript
- **UI库**: Vant 4（移动端组件库）
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **HTTP**: Axios
- **路由**: Vue Router 4

### 部署
- **容器**: Docker + Nginx
- **编排**: Docker Compose
- **CI/CD**: Jenkins
- **镜像仓库**: Harbor

## 📝 开发说明

### API 代理配置

#### 开发环境（vite.config.ts）
```typescript
server: {
  port: 3001,
  host: '0.0.0.0',
  proxy: {
    '/api': {
      target: 'http://localhost:45081',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/api')
    }
  }
}
```

#### 生产环境（nginx.conf）
```nginx
location /api/ {
  proxy_pass http://student-backend:45081/api/;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
}
```

### 自动导入配置

项目使用 `unplugin-auto-import` 和 `unplugin-vue-components`：

- 自动导入 Vue、Vue Router、Pinia API
- 自动注册 Vant 组件
- 无需手动导入即可使用

### 构建优化

- **Docker 多阶段构建**: 构建阶段和运行阶段分离
- **Nginx 静态服务**: 生产环境使用 Nginx 提供静态文件
- **Gzip 压缩**: 减少传输体积
- **缓存策略**: 静态资源长期缓存，HTML 禁用缓存

## 🔧 常见问题

### 问题 1: 端口冲突

```bash
# 检查端口占用
netstat -ano | findstr :45084

# 修改 docker-compose.yml 中的端口映射
ports:
  - "45085:80"  # 改为其他端口
```

### 问题 2: 容器无法启动

```bash
# 查看详细日志
docker compose logs student-mobile

# 检查健康状态
docker inspect student-course-mobile

# 重新构建并启动
docker compose up -d --force-recreate student-mobile
```

### 问题 3: API 请求失败

1. 检查后端服务是否运行
2. 验证 nginx 代理配置
3. 查看网络连接：`docker network inspect student-course-network`
4. 检查防火墙规则

### 问题 4: 构建失败

```bash
# 清理 Docker 缓存
docker builder prune

# 重新构建
cd student-course-mobile
docker build --no-cache -t student-course-mobile:latest .
```

## 📚 相关文档

- [主 Jenkins 配置文档](../JENKINS.md)
- [Jenkins 快速开始](../JENKINS_QUICK_START.md)
- [项目说明](../CLAUDE.md)
- [Docker Compose 配置](./docker-compose.yml)

## 🆘 支持

如遇问题：
1. 查看 Jenkins 控制台日志
2. 检查 Docker 容器日志
3. 参考本文档的故障排查部分
4. 联系运维团队

---

**创建日期**: 2025-10-21
**版本**: 1.0.0
**维护者**: DevOps Team
