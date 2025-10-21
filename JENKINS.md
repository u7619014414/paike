# Jenkins 多分支流水线配置文档

## 📋 概述

本项目包含三个独立的 Jenkins 流水线配置：

1. **主流水线** (`paike/Jenkinsfile`) - 多分支流水线，管理整个排课系统
2. **学生选课系统流水线** (`student-course/Jenkinsfile`) - 独立构建学生系统
3. **教师排课系统流水线** (`teacher-scheduling-system/Jenkinsfile`) - 独立构建教师系统

## 🏗️ 架构说明

```
paike/
├── Jenkinsfile                              # 主流水线（多分支）
├── student-course/
│   ├── Jenkinsfile                          # 学生系统流水线
│   ├── student-course-backend/
│   │   └── Dockerfile
│   ├── student-course-frontend/
│   │   └── Dockerfile
│   └── docker-compose.yml
├── teacher-scheduling-system/
│   ├── Jenkinsfile                          # 教师系统流水线
│   ├── backend/
│   │   └── Dockerfile
│   ├── frontend/
│   │   └── Dockerfile
│   └── docker-compose.yml
└── classroom-timeslot-management/
    └── (待添加 Dockerfile)
```

## 🚀 主流水线功能特性

### 多分支支持

主流水线支持以下分支：
- `develop` - 开发分支（默认）
- `master` - 生产分支
- `feature/*` - 功能分支

### 构建参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `BUILD_SCOPE` | Choice | ALL | 构建范围：ALL/STUDENT_COURSE/TEACHER_SYSTEM/CLASSROOM_MGMT |
| `SKIP_TESTS` | Boolean | false | 是否跳过测试 |
| `SKIP_DEPLOY` | Boolean | false | 是否跳过部署（仅构建镜像） |
| `CUSTOM_TAG` | String | 空 | 自定义镜像标签（留空则使用BUILD_NUMBER） |

### 环境变量

```groovy
GIT_REPO = "https://github.com/u7619014414/paike.git"
GIT_BRANCH = "${env.BRANCH_NAME ?: 'develop'}"
HARBOR_REGISTRY = "asdnn.com:45443"
HARBOR_PROJECT = "yl"
IMAGE_TAG = "${params.CUSTOM_TAG ?: env.BUILD_NUMBER}"
```

### 流水线阶段

#### 1. 📋 初始化与信息
- 显示构建信息
- 设置构建显示名称和描述

#### 2. 1️⃣ 代码同步
- 推送本地代码到 GitHub
- 自动提交并推送到指定分支

#### 3. 2️⃣ 并行构建
并行构建三个系统：
- **🎓 学生选课系统**
  - Maven 构建后端
  - Docker 构建前后端镜像
  - 镜像标签：`IMAGE_TAG`, `latest`, `BRANCH_NAME`

- **👨‍🏫 教师排课系统**
  - Maven 构建后端
  - Docker 构建前后端镜像
  - 镜像标签：`IMAGE_TAG`, `latest`, `BRANCH_NAME`

- **🏫 教室管理系统**
  - 仅前端 Docker 构建
  - 自动检查 Dockerfile 是否存在

#### 4. 3️⃣ 推送镜像到 Harbor
- 登录 Harbor 镜像仓库
- 根据 `BUILD_SCOPE` 推送相应镜像
- 推送三个标签：版本号、latest、分支名
- 支持重试机制（最多3次）
- 禁用代理以避免网络问题

#### 5. 4️⃣ 服务器部署
- 克隆最新代码到服务器
- 生成 `docker-compose.override.yml`
- 停止旧容器并启动新容器
- 健康检查和日志查看
- 清理未使用的镜像

## 📦 Docker 镜像命名规范

### 学生选课系统
- 后端：`asdnn.com:45443/yl/student-course-backend:TAG`
- 前端：`asdnn.com:45443/yl/student-course-frontend:TAG`

### 教师排课系统
- 后端：`asdnn.com:45443/yl/teacher-scheduling-backend:TAG`
- 前端：`asdnn.com:45443/yl/teacher-scheduling-frontend:TAG`

### 教室管理系统
- 前端：`asdnn.com:45443/yl/classroom-management:TAG`

其中 `TAG` 可以是：
- 构建号（如：`123`）
- `latest`
- 分支名（如：`develop`, `master`）

## 🔧 Jenkins 配置步骤

### 1. 创建多分支流水线任务

1. 登录 Jenkins
2. 点击 "New Item" / "新建任务"
3. 输入名称：`paike-multibranch-pipeline`
4. 选择 "Multibranch Pipeline" / "多分支流水线"
5. 点击 "OK"

### 2. 配置分支源

**Branch Sources** 部分：
- **Add source** → Git
- **Project Repository**: `https://github.com/u7619014414/paike.git`
- **Credentials**: 添加 GitHub Token（ID: `github-token`）

**Behaviours**:
- Discover branches
  - Strategy: All branches
- Discover tags
- Clean before checkout

**Property Strategy**:
- All branches get the same properties

### 3. 配置构建触发器

**Scan Multibranch Pipeline Triggers**:
- ✅ Periodically if not otherwise run
  - Interval: 5 minutes（或根据需要调整）

### 4. 配置 Jenkinsfile

**Build Configuration**:
- Mode: by Jenkinsfile
- Script Path: `Jenkinsfile`

### 5. 添加必需的凭据

在 Jenkins → Manage Jenkins → Credentials 中添加：

#### GitHub Token
- Kind: Secret text
- ID: `github-token`
- Secret: 你的 GitHub Personal Access Token
- Description: GitHub Token for paike

#### Harbor 凭据
- Kind: Username with password
- ID: `harbor-credentials`
- Username: Harbor 用户名
- Password: Harbor 密码
- Description: Harbor Registry Credentials

## 🔐 凭据配置详情

### GitHub Token 生成

1. 访问 https://github.com/settings/tokens
2. Generate new token (classic)
3. 选择权限：
   - ✅ repo (Full control of private repositories)
   - ✅ workflow
4. 复制生成的 token

### Harbor 凭据

确保 Harbor 用户有以下权限：
- 项目 `yl` 的 Developer 或 Master 权限
- 允许推送和拉取镜像

## 🌳 独立流水线使用

### 学生选课系统独立构建

1. 创建 Freestyle project：`student-course-pipeline`
2. Source Code Management → Git
   - Repository URL: `https://github.com/u7619014414/paike.git`
   - Branch: `*/develop`
3. Build → Pipeline script from SCM
   - Script Path: `student-course/Jenkinsfile`

### 教师排课系统独立构建

1. 创建 Freestyle project：`teacher-scheduling-pipeline`
2. Source Code Management → Git
   - Repository URL: `https://github.com/u7619014414/paike.git`
   - Branch: `*/develop`
3. Build → Pipeline script from SCM
   - Script Path: `teacher-scheduling-system/Jenkinsfile`

## 📊 使用示例

### 示例 1：完整构建和部署所有系统

```
参数配置：
- BUILD_SCOPE: ALL
- SKIP_TESTS: false
- SKIP_DEPLOY: false
- CUSTOM_TAG: (空)

结果：
- 构建所有三个系统
- 运行测试
- 推送镜像到 Harbor
- 部署到服务器
- 镜像标签：BUILD_NUMBER
```

### 示例 2：仅构建学生系统镜像（不部署）

```
参数配置：
- BUILD_SCOPE: STUDENT_COURSE
- SKIP_TESTS: true
- SKIP_DEPLOY: true
- CUSTOM_TAG: v1.2.0

结果：
- 仅构建学生选课系统
- 跳过测试
- 推送镜像到 Harbor
- 不部署到服务器
- 镜像标签：v1.2.0
```

### 示例 3：快速部署教师系统（跳过测试）

```
参数配置：
- BUILD_SCOPE: TEACHER_SYSTEM
- SKIP_TESTS: true
- SKIP_DEPLOY: false
- CUSTOM_TAG: hotfix-001

结果：
- 仅构建教师排课系统
- 跳过测试
- 推送镜像到 Harbor
- 部署到服务器
- 镜像标签：hotfix-001
```

## 🔍 构建后验证

### 检查镜像

```bash
# Windows (Jenkins 服务器)
docker images | findstr student-course
docker images | findstr teacher-scheduling
docker images | findstr classroom-management
```

### 检查 Harbor

访问：`https://asdnn.com:45443/harbor/projects/1/repositories`

应该看到：
- `yl/student-course-backend`
- `yl/student-course-frontend`
- `yl/teacher-scheduling-backend`
- `yl/teacher-scheduling-frontend`

### 检查服务器部署

```bash
# SSH 到服务器
ssh -i D:\1code\powershell\jenkins_id_rsa -p 45000 root@asdnn.com

# 检查学生选课系统
cd /data/1vueproj/paike/student-course
docker compose ps
docker compose logs --tail=50

# 检查教师排课系统
cd /data/1vueproj/paike/teacher-scheduling-system
docker compose ps
docker compose logs --tail=50
```

### 验证服务访问

- 学生选课系统前端：http://asdnn.com:3000
- 学生选课系统后端：http://asdnn.com:45081/api
- 学生选课系统后端健康检查：http://asdnn.com:45081/actuator/health

- 教师排课系统前端：http://asdnn.com:45100
- 教师排课系统后端：http://asdnn.com:45082/api
- 教师排课系统后端健康检查：http://asdnn.com:45082/api/actuator/health

- 教室管理系统：http://asdnn.com:45103

## 🐛 故障排查

### 问题 1: Maven 构建失败

```
错误：Maven 构建失败
解决：
1. 检查 pom.xml 配置
2. 确保 Java 17+ 已安装
3. 清理 Maven 缓存：mvn clean
4. 检查依赖是否可访问
```

### 问题 2: Docker 镜像推送失败

```
错误：docker push 失败，broken pipe
解决：
1. 确保禁用代理（已在 Jenkinsfile 中配置）
2. 检查 Harbor 服务是否正常
3. 验证 Harbor 凭据是否正确
4. 查看重试是否成功
```

### 问题 3: 服务器部署失败

```
错误：SSH 连接或部署脚本执行失败
解决：
1. 验证 SSH 密钥路径正确
2. 确保服务器 SSH 服务运行在端口 45000
3. 检查服务器磁盘空间
4. 查看服务器 Docker 日志
```

### 问题 4: 健康检查超时

```
错误：后端健康检查超时
解决：
1. 增加健康检查等待时间
2. 查看容器日志：docker compose logs backend
3. 检查数据库连接
4. 验证 application.yml 配置
```

### 问题 5: 并行构建冲突

```
错误：并行构建时资源冲突
解决：
1. 确保每个系统的端口不冲突
2. 检查 Docker 资源是否充足
3. 适当降低并行度（修改 Jenkinsfile 去掉 parallel）
```

## 📝 维护建议

### 定期清理

```groovy
// 在 Jenkinsfile 的 post always 中已配置
// 保留本地镜像以加速下次构建
// 仅清理未使用的镜像

docker image prune -f
```

### 日志管理

- Jenkins 构建日志保留：30 天
- Docker 容器日志轮转配置

### 备份策略

- 定期备份 Harbor 镜像仓库
- 备份 Jenkins 配置和凭据
- 备份数据库和配置文件

## 🔄 更新流程

### 更新 Jenkinsfile

1. 修改对应的 Jenkinsfile
2. 提交到 Git 仓库
3. Jenkins 自动扫描并应用新配置
4. 触发新的构建验证

### 添加新系统

1. 在项目中创建新的子目录
2. 添加 Dockerfile
3. 添加 docker-compose.yml
4. 更新根目录 Jenkinsfile 的并行构建阶段
5. 添加新的构建范围选项

### 修改镜像标签策略

如需修改镜像标签策略，编辑 Jenkinsfile 中的：
```groovy
docker tag IMAGE:${IMAGE_TAG} IMAGE:latest
docker tag IMAGE:${IMAGE_TAG} IMAGE:${GIT_BRANCH}
docker tag IMAGE:${IMAGE_TAG} IMAGE:custom-tag
```

## 📚 相关文档

- [Docker Compose 配置](./student-course/docker-compose.yml)
- [MyBatis Plus 迁移指南](./MYBATIS_PLUS_MIGRATION.md)
- [项目说明](./CLAUDE.md)
- [数据库 Schema](./database-schema.sql)

## 🆘 联系支持

如遇到问题：
1. 查看 Jenkins 控制台日志
2. 检查本文档的故障排查部分
3. 查看 Docker 容器日志
4. 联系运维团队

---

**最后更新**: 2025-10-21
**版本**: 1.0.0
**维护者**: Jenkins 管理员
