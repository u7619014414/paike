# Jenkins 多分支流水线 - 快速开始

## ✅ 已完成配置

本项目已完整配置Jenkins多分支CI/CD流水线，包含以下内容：

### 📁 创建的文件

```
paike/
├── Jenkinsfile                                    # ✅ 主流水线（支持多分支和参数化构建）
├── JENKINS.md                                     # ✅ 详细配置文档
├── JENKINS_SETUP_SUMMARY.md                       # ✅ 实施总结文档
├── JENKINS_QUICK_START.md                         # ✅ 本文件（快速开始）
├── verify-jenkins-setup.bat                       # ✅ Windows验证脚本
├── verify-jenkins-setup.sh                        # ✅ Linux验证脚本
│
├── student-course/
│   └── Jenkinsfile                                # ✅ 学生系统独立流水线
│
├── teacher-scheduling-system/
│   └── Jenkinsfile                                # ✅ 教师系统独立流水线
│
└── classroom-timeslot-management/
    ├── Dockerfile                                 # ✅ 新建
    ├── nginx.conf                                 # ✅ 新建
    ├── .dockerignore                              # ✅ 新建
    └── docker-compose.yml                         # ✅ 新建
```

## 🚀 5分钟快速开始

### 步骤 1: 提交代码到 GitHub

```bash
# 在 paike 目录下执行
git add .
git commit -m "feat: add Jenkins multibranch pipeline configuration"
git push origin develop
```

### 步骤 2: 在 Jenkins 创建多分支流水线

1. **登录 Jenkins** → 点击 "New Item"
2. **输入名称**: `paike-multibranch-pipeline`
3. **选择类型**: "Multibranch Pipeline"
4. **Branch Sources**:
   - Add source → Git
   - Project Repository: `https://github.com/u7619014414/paike.git`
   - Credentials: 选择或添加 `github-token`
5. **Build Configuration**:
   - Mode: by Jenkinsfile
   - Script Path: `Jenkinsfile`
6. **保存**

### 步骤 3: 添加必需的凭据

在 Jenkins → Manage Jenkins → Credentials 中添加：

#### 1. GitHub Token
```
Domain: Global
Kind: Secret text
Scope: Global
Secret: [你的GitHub Personal Access Token]
ID: github-token
Description: GitHub Token for paike
```

生成Token: https://github.com/settings/tokens
需要权限: `repo`, `workflow`

#### 2. Harbor 凭据
```
Domain: Global
Kind: Username with password
Scope: Global
Username: [Harbor用户名]
Password: [Harbor密码]
ID: harbor-credentials
Description: Harbor Registry Credentials
```

### 步骤 4: 触发第一次构建

1. 回到 Jenkins → `paike-multibranch-pipeline`
2. 等待 Jenkins 自动扫描分支（或点击 "Scan Multibranch Pipeline Now"）
3. 找到 `develop` 分支 → 点击 "Build with Parameters"
4. 选择参数:
   - **BUILD_SCOPE**: `STUDENT_COURSE` (先测试单个系统)
   - **SKIP_TESTS**: `true` (加快首次构建)
   - **SKIP_DEPLOY**: `true` (仅构建镜像)
   - **CUSTOM_TAG**: `test-001`
5. 点击 "Build"

### 步骤 5: 验证构建结果

#### 查看 Jenkins 日志
点击构建号 → Console Output → 应该看到：
```
✅ 构建成功！（仅构建，未部署）
镜像标签: test-001
```

#### 检查 Harbor 镜像
访问: `https://asdnn.com:45443/harbor/projects/1/repositories`

应该看到新的镜像:
- `yl/student-course-backend:test-001`
- `yl/student-course-frontend:test-001`

## 📖 使用流水线

### 场景 1: 部署学生系统到测试环境

```
参数:
- BUILD_SCOPE: STUDENT_COURSE
- SKIP_TESTS: false
- SKIP_DEPLOY: false
- CUSTOM_TAG: (留空)

结果: 构建、测试、推送镜像并部署
```

### 场景 2: 完整部署所有系统

```
参数:
- BUILD_SCOPE: ALL
- SKIP_TESTS: false
- SKIP_DEPLOY: false
- CUSTOM_TAG: v1.0.0

结果: 所有三个系统都构建、测试、推送、部署
```

### 场景 3: 仅构建镜像不部署

```
参数:
- BUILD_SCOPE: ALL
- SKIP_TESTS: true
- SKIP_DEPLOY: true
- CUSTOM_TAG: dev-build

结果: 快速构建所有镜像，跳过测试和部署
```

## 🎯 三种流水线模式

### 模式 1: 主流水线（推荐）
- **文件**: `paike/Jenkinsfile`
- **用途**: 统一管理所有系统
- **优势**: 并行构建、参数化、多分支支持

### 模式 2: 学生系统独立流水线
- **文件**: `student-course/Jenkinsfile`
- **用途**: 仅构建学生系统
- **优势**: 独立发布、快速部署

### 模式 3: 教师系统独立流水线
- **文件**: `teacher-scheduling-system/Jenkinsfile`
- **用途**: 仅构建教师系统
- **优势**: 独立发布、快速部署

## 🔍 验证部署

### 检查服务器容器

```bash
# SSH 到服务器
ssh -i D:\1code\powershell\jenkins_id_rsa -p 45000 root@asdnn.com

# 检查学生系统
cd /data/1vueproj/paike/student-course
docker compose ps
docker compose logs --tail=50

# 检查教师系统
cd /data/1vueproj/paike/teacher-scheduling-system
docker compose ps
docker compose logs --tail=50
```

### 访问应用

- 学生选课系统: http://asdnn.com:3000
- 教师排课系统: http://asdnn.com:45100
- 教室管理系统: http://asdnn.com:45103

### 健康检查

- 学生后端: http://asdnn.com:45081/actuator/health
- 教师后端: http://asdnn.com:45082/api/actuator/health

## 📊 构建参数说明

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| `BUILD_SCOPE` | 构建哪个系统 | 开发:`STUDENT_COURSE`, 生产:`ALL` |
| `SKIP_TESTS` | 跳过测试 | 开发:`true`, 生产:`false` |
| `SKIP_DEPLOY` | 跳过部署 | 仅构建镜像时:`true` |
| `CUSTOM_TAG` | 镜像标签 | 生产:`v1.0.0`, 开发:留空 |

## 🐛 常见问题

### Q: Maven 构建失败？
```bash
A: 检查 Java 版本是否为 17+
   清理缓存: mvn clean
```

### Q: Docker 推送失败？
```bash
A: 1. 检查 Harbor 凭据是否正确
   2. 确认代理已禁用（Jenkinsfile已配置）
   3. 查看是否使用了重试机制
```

### Q: 健康检查超时？
```bash
A: 1. 增加等待时间（deploy.sh中已配置30秒）
   2. 检查数据库是否正常启动
   3. 查看容器日志: docker compose logs backend
```

### Q: 如何回滚版本？
```bash
A: 1. 在Jenkins中找到之前的构建号
   2. 使用CUSTOM_TAG指定该构建号重新构建
   或在服务器上修改docker-compose.override.yml
```

## 📚 详细文档

- [JENKINS.md](./JENKINS.md) - 详细配置指南和故障排查
- [JENKINS_SETUP_SUMMARY.md](./JENKINS_SETUP_SUMMARY.md) - 完整实施总结
- [CLAUDE.md](./CLAUDE.md) - 项目说明

## 🎓 最佳实践

### 分支策略
```
master   → 生产环境 (CUSTOM_TAG=v1.0.0)
develop  → 测试环境 (默认BUILD_NUMBER)
feature/* → 开发环境 (SKIP_DEPLOY=true)
```

### 构建频率
```
开发阶段: 按需手动触发
测试阶段: 每次提交自动触发
生产发布: 手动触发，使用版本标签
```

### 标签规范
```
v1.0.0     → 正式发布
v1.1.0-rc1 → 候选版本
hotfix-*   → 热修复
dev-*      → 开发测试
```

## ✨ 下一步

1. ✅ 配置 Jenkins 多分支流水线
2. ✅ 添加必需的凭据
3. ✅ 运行第一次测试构建
4. 📝 配置 GitHub Webhook (自动触发构建)
5. 📝 设置构建结果通知 (邮件/Slack)
6. 📝 配置定期清理旧镜像策略

## 🆘 需要帮助？

- 查看详细文档: [JENKINS.md](./JENKINS.md)
- 故障排查: 见 JENKINS.md 第 "故障排查" 章节
- 运行验证脚本: `verify-jenkins-setup.bat` (Windows) 或 `./verify-jenkins-setup.sh` (Linux)

---

**创建日期**: 2025-10-21
**最后更新**: 2025-10-21
**维护者**: DevOps Team

🚀 Happy Building!
