# Jenkins 多分支流水线实施总结

## 📊 实施概览

本次实施为排课系统创建了完整的多分支 Jenkins CI/CD 流水线，支持三个子系统的独立或统一构建部署。

## ✅ 已完成的工作

### 1. 创建的文件清单

```
paike/
├── Jenkinsfile                                    # ✅ 主流水线（多分支）
├── JENKINS.md                                     # ✅ Jenkins配置文档
├── JENKINS_SETUP_SUMMARY.md                       # ✅ 本文件
│
├── student-course/
│   └── Jenkinsfile                                # ✅ 学生系统独立流水线
│
├── teacher-scheduling-system/
│   └── Jenkinsfile                                # ✅ 教师系统独立流水线（已存在）
│
└── classroom-timeslot-management/
    ├── Dockerfile                                 # ✅ 新建
    ├── nginx.conf                                 # ✅ 新建
    ├── .dockerignore                              # ✅ 新建
    └── docker-compose.yml                         # ✅ 新建
```

### 2. 三种流水线模式

#### 模式 1: 主流水线（推荐用于生产）
- **文件**: `paike/Jenkinsfile`
- **类型**: 多分支流水线
- **特点**:
  - 支持多分支（develop/master/feature/*）
  - 并行构建三个系统
  - 灵活的参数控制
  - 统一的镜像管理

#### 模式 2: 学生系统独立流水线
- **文件**: `student-course/Jenkinsfile`
- **用途**: 仅构建和部署学生选课系统
- **包含**:
  - 学生后端（Spring Boot + MyBatis Plus）
  - 学生前端（Vue 3 + Element Plus）
  - MySQL 数据库
  - Redis 缓存

#### 模式 3: 教师系统独立流水线
- **文件**: `teacher-scheduling-system/Jenkinsfile`
- **用途**: 仅构建和部署教师排课系统
- **包含**:
  - 教师后端（Spring Boot + MyBatis Plus）
  - 教师前端（Vue 3 + Element Plus）

### 3. Docker 镜像仓库结构

所有镜像推送到 Harbor: `asdnn.com:45443/yl/`

```
yl/
├── student-course-backend:TAG         # 学生后端
├── student-course-frontend:TAG        # 学生前端
├── teacher-scheduling-backend:TAG     # 教师后端
├── teacher-scheduling-frontend:TAG    # 教师前端
└── classroom-management:TAG           # 教室管理前端
```

每个镜像有三个标签：
- `构建号` (如: 123)
- `latest`
- `分支名` (如: develop, master)

## 🎯 主流水线功能特性

### 构建参数

| 参数 | 选项 | 说明 |
|------|------|------|
| `BUILD_SCOPE` | ALL / STUDENT_COURSE / TEACHER_SYSTEM / CLASSROOM_MGMT | 选择构建哪个系统 |
| `SKIP_TESTS` | true / false | 是否跳过 Maven 测试 |
| `SKIP_DEPLOY` | true / false | 是否跳过部署（仅构建镜像） |
| `CUSTOM_TAG` | 自定义字符串 | 自定义镜像标签 |

### 流水线阶段

```
1. 📋 初始化与信息
   └─ 显示构建信息，设置显示名称

2. 1️⃣ 代码同步
   └─ 推送本地代码到 GitHub

3. 2️⃣ 并行构建
   ├─ 🎓 学生选课系统
   │   ├─ Maven 构建后端
   │   ├─ Docker 构建后端镜像
   │   └─ Docker 构建前端镜像
   ├─ 👨‍🏫 教师排课系统
   │   ├─ Maven 构建后端
   │   ├─ Docker 构建后端镜像
   │   └─ Docker 构建前端镜像
   └─ 🏫 教室管理系统
       └─ Docker 构建前端镜像

4. 3️⃣ 推送镜像到 Harbor
   └─ 登录并推送所有构建的镜像

5. 4️⃣ 服务器部署
   └─ SSH 到服务器，拉取镜像并启动容器
```

## 🚀 快速开始指南

### 步骤 1: 在 Jenkins 中创建多分支流水线

```bash
# 1. Jenkins → New Item
# 2. 输入名称: paike-multibranch-pipeline
# 3. 选择: Multibranch Pipeline
# 4. 配置分支源:
#    - Git URL: https://github.com/u7619014414/paike.git
#    - Credentials: github-token
# 5. Build Configuration:
#    - Script Path: Jenkinsfile
```

### 步骤 2: 添加凭据

在 Jenkins → Manage Jenkins → Credentials 中添加：

1. **GitHub Token** (ID: `github-token`)
   - Type: Secret text
   - 生成位置: https://github.com/settings/tokens

2. **Harbor 凭据** (ID: `harbor-credentials`)
   - Type: Username with password
   - Username: Harbor 用户名
   - Password: Harbor 密码

### 步骤 3: 触发构建

方式 1: 手动触发
```
Jenkins → paike-multibranch-pipeline → develop → Build with Parameters
选择参数后点击 Build
```

方式 2: Git 推送触发（需配置 Webhook）
```bash
git push origin develop
# Jenkins 自动检测并构建
```

## 📖 使用场景示例

### 场景 1: 完整部署所有系统（生产发布）

```
参数:
- BUILD_SCOPE: ALL
- SKIP_TESTS: false
- SKIP_DEPLOY: false
- CUSTOM_TAG: v1.0.0

结果:
✅ 构建所有三个系统
✅ 运行所有测试
✅ 推送镜像（标签: v1.0.0, latest, develop）
✅ 部署到服务器
```

### 场景 2: 仅构建学生系统（开发测试）

```
参数:
- BUILD_SCOPE: STUDENT_COURSE
- SKIP_TESTS: true
- SKIP_DEPLOY: false
- CUSTOM_TAG: dev-test-001

结果:
✅ 仅构建学生选课系统
⏭️  跳过测试
✅ 推送镜像（标签: dev-test-001, latest, develop）
✅ 部署到服务器
```

### 场景 3: 构建镜像但不部署（镜像准备）

```
参数:
- BUILD_SCOPE: ALL
- SKIP_TESTS: true
- SKIP_DEPLOY: true
- CUSTOM_TAG: (留空，使用BUILD_NUMBER)

结果:
✅ 构建所有三个系统
⏭️  跳过测试
✅ 推送镜像到 Harbor
⏭️  不部署到服务器
```

### 场景 4: 热修复教师系统（紧急修复）

```
参数:
- BUILD_SCOPE: TEACHER_SYSTEM
- SKIP_TESTS: false
- SKIP_DEPLOY: false
- CUSTOM_TAG: hotfix-login-bug

结果:
✅ 仅构建教师排课系统
✅ 运行测试确保修复有效
✅ 推送镜像（标签: hotfix-login-bug, latest, develop）
✅ 立即部署到服务器
```

## 🔍 验证部署成功

### 1. 检查 Jenkins 构建日志

```
Jenkins Console Output 应显示:
========================================
✅ 构建成功！
========================================
分支: develop
构建号: 123
镜像标签: 123
学生选课系统前端: http://asdnn.com:3000
学生选课系统后端: http://asdnn.com:45081/api
教师排课系统前端: http://asdnn.com:45100
教师排课系统后端: http://asdnn.com:45082/api
教室管理系统: http://asdnn.com:45103
========================================
```

### 2. 检查 Harbor 镜像

访问: https://asdnn.com:45443/harbor/projects/1/repositories

应看到所有镜像及其标签。

### 3. 检查服务器容器

```bash
# SSH 到服务器
ssh -i D:\1code\powershell\jenkins_id_rsa -p 45000 root@asdnn.com

# 检查学生系统
cd /data/1vueproj/paike/student-course
docker compose ps
docker compose logs --tail=50 student-backend

# 检查教师系统
cd /data/1vueproj/paike/teacher-scheduling-system
docker compose ps
docker compose logs --tail=50 teacher-backend

# 检查教室管理系统
cd /data/1vueproj/paike/classroom-timeslot-management
docker compose ps
docker compose logs --tail=20 classroom-management
```

### 4. 访问应用

浏览器访问:
- http://asdnn.com:3000 - 学生选课系统
- http://asdnn.com:45100 - 教师排课系统
- http://asdnn.com:45103 - 教室管理系统

健康检查:
- http://asdnn.com:45081/actuator/health - 学生后端
- http://asdnn.com:45082/api/actuator/health - 教师后端

## 🛠️ 常见操作

### 手动部署特定镜像版本

```bash
# SSH 到服务器
ssh -i D:\1code\powershell\jenkins_id_rsa -p 45000 root@asdnn.com

cd /data/1vueproj/paike/student-course

# 创建 override 文件指定版本
cat > docker-compose.override.yml <<EOF
services:
  student-backend:
    image: asdnn.com:45443/yl/student-course-backend:123
  student-frontend:
    image: asdnn.com:45443/yl/student-course-frontend:123
EOF

# 重启服务
docker compose down
docker compose pull
docker compose up -d
```

### 回滚到上一版本

```bash
# 在 Jenkins 中找到上一个成功的构建号（如：122）
# 然后使用 CUSTOM_TAG=122 参数重新构建
# 或直接在服务器上修改 docker-compose.override.yml
```

### 查看容器日志

```bash
# 实时日志
docker compose logs -f student-backend

# 最近100行
docker compose logs --tail=100 teacher-backend

# 所有服务日志
docker compose logs
```

### 重启单个服务

```bash
# 重启学生后端
docker compose restart student-backend

# 重启并拉取最新镜像
docker compose pull student-backend
docker compose up -d --force-recreate student-backend
```

## 📊 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                    Jenkins (Windows)                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │         Multibranch Pipeline (主流水线)           │  │
│  │  ┌─────────┐  ┌─────────┐  ┌──────────────────┐  │  │
│  │  │ develop │  │ master  │  │   feature/*      │  │  │
│  │  └────┬────┘  └────┬────┘  └────────┬─────────┘  │  │
│  └───────┼────────────┼────────────────┼────────────┘  │
│          │            │                │                │
│  ┌───────▼────────────▼────────────────▼────────────┐  │
│  │              并行构建阶段                        │  │
│  │  ┌────────┐  ┌────────┐  ┌───────────────────┐  │  │
│  │  │ 学生系统│  │ 教师系统│  │  教室管理系统      │  │  │
│  │  │ Frontend│  │ Frontend│  │    Frontend       │  │  │
│  │  │ Backend │  │ Backend │  └───────────────────┘  │  │
│  │  └────┬───┘  └────┬───┘                          │  │
│  └───────┼───────────┼──────────────────────────────┘  │
└──────────┼───────────┼─────────────────────────────────┘
           │           │
           ▼           ▼
    ┌──────────────────────────┐
    │   Harbor (镜像仓库)       │
    │   asdnn.com:45443        │
    │  ┌────────────────────┐  │
    │  │ yl/student-*:TAG   │  │
    │  │ yl/teacher-*:TAG   │  │
    │  │ yl/classroom-*:TAG │  │
    │  └────────────────────┘  │
    └──────────┬───────────────┘
               │ docker pull
               ▼
    ┌──────────────────────────┐
    │  部署服务器 (Linux)       │
    │  asdnn.com:45000         │
    │  ┌────────────────────┐  │
    │  │ student-course/    │  │
    │  │  ├─ MySQL :45306   │  │
    │  │  ├─ Redis :45379   │  │
    │  │  ├─ Backend :45081 │  │
    │  │  └─ Frontend :3000  │  │
    │  ├────────────────────┤  │
    │  │ teacher-system/    │  │
    │  │  ├─ Backend :45082 │  │
    │  │  └─ Frontend :45100│  │
    │  ├────────────────────┤  │
    │  │ classroom-mgmt/    │  │
    │  │  └─ Frontend :45103│  │
    │  └────────────────────┘  │
    └──────────────────────────┘
```

## 🔐 安全考虑

### 凭据管理
- ✅ GitHub Token 存储在 Jenkins 凭据中
- ✅ Harbor 密码存储在 Jenkins 凭据中
- ✅ SSH 私钥文件权限正确设置
- ⚠️ 定期轮换凭据

### 网络安全
- ✅ 禁用代理访问内网 Harbor
- ✅ 使用 SSH 密钥认证
- ✅ Harbor 使用 HTTPS (端口 45443)

### 镜像安全
- ✅ 使用官方基础镜像（node:18-alpine, nginx:alpine）
- ✅ 多阶段构建减少镜像体积
- ⚠️ 建议定期扫描镜像漏洞

## 📈 性能优化建议

### 构建优化
1. **并行构建**: 已实现，三个系统同时构建
2. **Docker 层缓存**: 保留本地镜像加速构建
3. **跳过测试选项**: 开发环境可选择跳过
4. **增量构建**: Maven 使用本地缓存

### 部署优化
1. **健康检查**: 所有服务都有健康检查配置
2. **优雅启动**: 等待数据库就绪后再启动后端
3. **日志管理**: 使用 docker-compose logs 管理
4. **资源限制**: 可在 docker-compose.yml 中添加

## 🎓 最佳实践

### 分支管理
```
master    → 生产环境（使用 CUSTOM_TAG=v1.0.0）
develop   → 测试环境（使用默认 BUILD_NUMBER）
feature/* → 开发环境（使用 SKIP_DEPLOY=true）
```

### 版本标签
```
v1.0.0    → 生产发布版本
v1.1.0-rc1 → 候选发布版本
hotfix-*  → 热修复版本
dev-*     → 开发测试版本
```

### 构建流程
```
1. 开发阶段:
   - 分支: feature/new-function
   - 参数: BUILD_SCOPE=STUDENT_COURSE, SKIP_TESTS=true, SKIP_DEPLOY=true

2. 测试阶段:
   - 分支: develop
   - 参数: BUILD_SCOPE=ALL, SKIP_TESTS=false, SKIP_DEPLOY=false

3. 生产发布:
   - 分支: master
   - 参数: BUILD_SCOPE=ALL, SKIP_TESTS=false, CUSTOM_TAG=v1.0.0
```

## 📚 相关文档

- [详细 Jenkins 配置指南](./JENKINS.md)
- [项目说明](./CLAUDE.md)
- [MyBatis Plus 迁移文档](./MYBATIS_PLUS_MIGRATION.md)
- [数据库 Schema](./database-schema.sql)

## 🆘 故障排查快速参考

| 问题 | 快速解决方案 |
|------|-------------|
| Maven 构建失败 | 检查 Java 版本，清理缓存: `mvn clean` |
| Docker 推送失败 | 检查 Harbor 凭据，确认代理已禁用 |
| 健康检查超时 | 增加等待时间，检查数据库连接 |
| SSH 连接失败 | 验证密钥路径和权限 |
| 并行构建冲突 | 检查 Docker 资源，考虑串行构建 |

## 📞 支持与联系

如遇到问题:
1. 查看 Jenkins 控制台日志
2. 检查 [JENKINS.md](./JENKINS.md) 故障排查章节
3. 查看服务器 Docker 日志
4. 联系 DevOps 团队

---

**创建日期**: 2025-10-21
**版本**: 1.0.0
**维护者**: DevOps Team
**最后更新**: 2025-10-21
