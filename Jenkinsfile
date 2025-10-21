/**
 * 排课系统多分支流水线 - 主Jenkinsfile
 *
 * 该流水线管理整个排课系统的构建和部署，包括：
 * - student-course: 学生选课系统（前端 + 后端）
 * - teacher-scheduling-system: 教师排课系统（前端 + 后端）
 * - classroom-timeslot-management: 教室时间段管理（前端）
 *
 * 支持多分支构建：develop、master、feature/*
 */

pipeline {
    agent any

    parameters {
        choice(
            name: 'BUILD_SCOPE',
            choices: ['ALL', 'STUDENT_COURSE', 'TEACHER_SYSTEM', 'CLASSROOM_MGMT'],
            description: '选择构建范围：ALL=全部系统, STUDENT_COURSE=学生系统, TEACHER_SYSTEM=教师系统, CLASSROOM_MGMT=教室管理'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: '是否跳过测试'
        )
        booleanParam(
            name: 'SKIP_DEPLOY',
            defaultValue: false,
            description: '是否跳过部署（仅构建镜像）'
        )
        string(
            name: 'CUSTOM_TAG',
            defaultValue: '',
            description: '自定义镜像标签（留空则使用BUILD_NUMBER）'
        )
    }

    environment {
        GIT_REPO = "https://github.com/u7619014414/paike.git"
        GIT_BRANCH = "${env.BRANCH_NAME ?: 'develop'}"

        // 本地代码路径
        LOCAL_CODE_DIR = "D:\\1vueproj\\paike"

        // Harbor 镜像仓库配置
        HARBOR_REGISTRY = "asdnn.com:45443"
        HARBOR_PROJECT = "yl"
        IMAGE_TAG = "${params.CUSTOM_TAG ?: env.BUILD_NUMBER}"

        // 服务器部署路径
        SERVER_DEPLOY_DIR = "/data/1vueproj"

        // SSH 配置
        SSH_KEY = "D:\\1code\\powershell\\jenkins_id_rsa"
        SSH_PORT = "45000"
        SSH_HOST = "asdnn.com"
        SSH_USER = "root"
        SSH_BIN = "D:\\soft\\Git\\usr\\bin\\ssh.exe"
        SCP_BIN = "D:\\soft\\Git\\usr\\bin\\scp.exe"

        // Maven 配置
        MAVEN_OPTS = "-DskipTests=${params.SKIP_TESTS}"
    }

    stages {
        stage('📋 初始化与信息') {
            steps {
                script {
                    echo """
========================================
🚀 排课系统多分支流水线启动
========================================
分支: ${GIT_BRANCH}
构建号: ${env.BUILD_NUMBER}
镜像标签: ${IMAGE_TAG}
构建范围: ${params.BUILD_SCOPE}
跳过测试: ${params.SKIP_TESTS}
跳过部署: ${params.SKIP_DEPLOY}
========================================
"""
                    // 设置构建显示名称
                    currentBuild.displayName = "#${env.BUILD_NUMBER} [${GIT_BRANCH}] ${params.BUILD_SCOPE}"
                    currentBuild.description = "Tag: ${IMAGE_TAG}"
                }
            }
        }

        stage('1️⃣ 代码同步') {
            steps {
                echo "==== [1] 推送本地代码到 GitHub ===="
                dir("${LOCAL_CODE_DIR}") {
                    withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                        bat """
                            git add .
                            git commit -m "Jenkins Build #${env.BUILD_NUMBER} [${GIT_BRANCH}] - Auto commit" || echo "No changes to commit"
                            git remote set-url origin https://u7619014414:%GITHUB_TOKEN%@github.com/u7619014414/paike.git
                            git push origin ${GIT_BRANCH}
                        """
                    }
                }
            }
        }

        stage('2️⃣ 并行构建') {
            parallel {
                // ========== 学生选课系统 ==========
                stage('🎓 学生选课系统') {
                    when {
                        expression { params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'STUDENT_COURSE' }
                    }
                    stages {
                        stage('学生后端构建') {
                            steps {
                                echo "==== 构建学生选课后端 ===="
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\student-course\\student-course-backend"
                                    echo 📦 Maven 构建...
                                    call mvn clean package ${MAVEN_OPTS}
                                    if errorlevel 1 exit /b 1

                                    echo 🐳 Docker 镜像构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:${GIT_BRANCH}

                                    echo ✅ 学生后端镜像构建完成
                                """
                            }
                        }

                        stage('学生前端构建') {
                            steps {
                                echo "==== 构建学生选课前端 ===="
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\student-course\\student-course-frontend"
                                    echo 🐳 Docker 多阶段构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:${GIT_BRANCH}

                                    echo ✅ 学生前端镜像构建完成
                                """
                            }
                        }

                        stage('学生移动端构建') {
                            steps {
                                echo "==== 构建学生选课移动端 ===="
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\student-course\\student-course-mobile"
                                    echo 🐳 Docker 多阶段构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${GIT_BRANCH}

                                    echo ✅ 学生移动端镜像构建完成
                                """
                            }
                        }
                    }
                }

                // ========== 教师排课系统 ==========
                stage('👨‍🏫 教师排课系统') {
                    when {
                        expression { params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'TEACHER_SYSTEM' }
                    }
                    stages {
                        stage('教师后端构建') {
                            steps {
                                echo "==== 构建教师排课后端 ===="
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\teacher-scheduling-system\\backend"
                                    echo 📦 Maven 构建...
                                    call mvn clean package ${MAVEN_OPTS}
                                    if errorlevel 1 exit /b 1

                                    echo 🐳 Docker 镜像构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:${GIT_BRANCH}

                                    echo ✅ 教师后端镜像构建完成
                                """
                            }
                        }

                        stage('教师前端构建') {
                            steps {
                                echo "==== 构建教师排课前端 ===="
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\teacher-scheduling-system\\frontend"
                                    echo 🐳 Docker 多阶段构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:${GIT_BRANCH}

                                    echo ✅ 教师前端镜像构建完成
                                """
                            }
                        }
                    }
                }

                // ========== 教室时间段管理 ==========
                stage('🏫 教室管理系统') {
                    when {
                        expression { params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'CLASSROOM_MGMT' }
                    }
                    steps {
                        echo "==== 构建教室时间段管理前端 ===="
                        script {
                            // 检查是否存在 Dockerfile
                            def hasDockerfile = fileExists("${LOCAL_CODE_DIR}\\classroom-timeslot-management\\Dockerfile")

                            if (hasDockerfile) {
                                bat """
                                    cd /d "${LOCAL_CODE_DIR}\\classroom-timeslot-management"
                                    echo 🐳 Docker 多阶段构建...
                                    docker build -t ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/classroom-management:${IMAGE_TAG} .
                                    if errorlevel 1 exit /b 1

                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/classroom-management:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/classroom-management:latest
                                    docker tag ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/classroom-management:${IMAGE_TAG} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/classroom-management:${GIT_BRANCH}

                                    echo ✅ 教室管理前端镜像构建完成
                                """
                            } else {
                                echo "⚠️  Dockerfile 不存在，跳过教室管理系统构建"
                                echo "请先创建 classroom-timeslot-management/Dockerfile"
                            }
                        }
                    }
                }
            }
        }

        stage('3️⃣ 推送镜像到 Harbor') {
            when {
                expression { !params.SKIP_DEPLOY }
            }
            steps {
                echo "==== [3] 推送所有 Docker 镜像到 Harbor ===="
                script {
                    withCredentials([usernamePassword(credentialsId: 'harbor-credentials', usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
                        // 登录 Harbor
                        bat """
                            echo 🔐 登录 Harbor...
                            set HTTP_PROXY=
                            set HTTPS_PROXY=
                            set NO_PROXY=asdnn.com,localhost,127.0.0.1
                            docker login ${HARBOR_REGISTRY} -u %HARBOR_USER% -p %HARBOR_PASS%
                        """

                        // 根据构建范围推送镜像
                        if (params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'STUDENT_COURSE') {
                            echo "📤 推送学生选课系统镜像..."

                            def images = [
                                "student-course-backend:${IMAGE_TAG}",
                                "student-course-backend:latest",
                                "student-course-backend:${GIT_BRANCH}",
                                "student-course-frontend:${IMAGE_TAG}",
                                "student-course-frontend:latest",
                                "student-course-frontend:${GIT_BRANCH}",
                                "student-course-mobile:${IMAGE_TAG}",
                                "student-course-mobile:latest",
                                "student-course-mobile:${GIT_BRANCH}"
                            ]

                            images.each { image ->
                                retry(3) {
                                    bat """
                                        set HTTP_PROXY=
                                        set HTTPS_PROXY=
                                        set NO_PROXY=asdnn.com,localhost,127.0.0.1
                                        echo 📤 推送 ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image}
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image} || exit /b 1
                                    """
                                }
                            }
                        }

                        if (params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'TEACHER_SYSTEM') {
                            echo "📤 推送教师排课系统镜像..."

                            def images = [
                                "teacher-scheduling-backend:${IMAGE_TAG}",
                                "teacher-scheduling-backend:latest",
                                "teacher-scheduling-backend:${GIT_BRANCH}",
                                "teacher-scheduling-frontend:${IMAGE_TAG}",
                                "teacher-scheduling-frontend:latest",
                                "teacher-scheduling-frontend:${GIT_BRANCH}"
                            ]

                            images.each { image ->
                                retry(3) {
                                    bat """
                                        set HTTP_PROXY=
                                        set HTTPS_PROXY=
                                        set NO_PROXY=asdnn.com,localhost,127.0.0.1
                                        echo 📤 推送 ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image}
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image} || exit /b 1
                                    """
                                }
                            }
                        }

                        if (params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'CLASSROOM_MGMT') {
                            def hasClassroomImage = bat(
                                script: "docker images | findstr classroom-management",
                                returnStatus: true
                            ) == 0

                            if (hasClassroomImage) {
                                echo "📤 推送教室管理系统镜像..."

                                def images = [
                                    "classroom-management:${IMAGE_TAG}",
                                    "classroom-management:latest",
                                    "classroom-management:${GIT_BRANCH}"
                                ]

                                images.each { image ->
                                    retry(3) {
                                        bat """
                                            set HTTP_PROXY=
                                            set HTTPS_PROXY=
                                            set NO_PROXY=asdnn.com,localhost,127.0.0.1
                                            echo 📤 推送 ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image}
                                            docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${image} || exit /b 1
                                        """
                                    }
                                }
                            } else {
                                echo "⚠️  教室管理系统镜像不存在，跳过推送"
                            }
                        }

                        echo "✅ 所有镜像推送完成"
                    }
                }
            }
        }

        stage('4️⃣ 服务器部署') {
            when {
                expression { !params.SKIP_DEPLOY }
            }
            steps {
                echo "==== [4] 部署到服务器 ===="
                script {
                    // 生成统一部署脚本
                    writeFile file: 'deploy-all.sh', text: """#!/bin/bash
set -e

echo "=========================================="
echo "排课系统部署"
echo "分支: ${GIT_BRANCH}"
echo "构建号: ${IMAGE_TAG}"
echo "构建范围: ${params.BUILD_SCOPE}"
echo "=========================================="

# 进入部署目录
cd ${SERVER_DEPLOY_DIR}

# 删除旧代码
echo "🗑️  删除旧代码..."
rm -rf paike

# 克隆最新代码（带重试和优化配置）
echo "📥 克隆最新代码..."
export GIT_HTTP_MAX_REQUEST_BUFFER=100M
export GIT_HTTP_POST_BUFFER=100M
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999

# 尝试克隆，最多重试 3 次
CLONE_SUCCESS=false
for i in 1 2 3; do
    echo "📥 克隆尝试 \$i/3..."
    if git clone --depth 1 -b ${GIT_BRANCH} ${GIT_REPO}; then
        CLONE_SUCCESS=true
        echo "✅ 克隆成功"
        break
    else
        echo "⚠️  克隆失败，等待 5 秒后重试..."
        sleep 5
    fi
done

if [ "\$CLONE_SUCCESS" = false ]; then
    echo "❌ 克隆失败，尝试使用本地已有代码"
    # 这里可以添加备用方案，比如使用缓存的代码
    exit 1
fi

# 函数：部署单个系统
deploy_system() {
    local system_name=\$1
    local compose_dir=\$2
    local override_file=\$3

    echo ""
    echo "=========================================="
    echo "部署 \${system_name}"
    echo "=========================================="

    cd \${compose_dir}

    if [ -f "/tmp/\${override_file}" ]; then
        cp /tmp/\${override_file} ./docker-compose.override.yml
        echo "✅ 已应用镜像版本配置"
    fi

    echo "⏹️  停止旧容器..."
    docker compose down -v || true

    echo "📥 拉取最新镜像..."
    docker compose pull

    echo "🚀 启动新容器..."
    docker compose up -d

    echo "⏳ 等待容器启动（20秒）..."
    sleep 20

    echo "📊 容器状态："
    docker compose ps

    echo "📜 日志（最后20行）："
    docker compose logs --tail=20
}

# 根据构建范围部署
BUILD_SCOPE="${params.BUILD_SCOPE}"

if [ "\$BUILD_SCOPE" = "ALL" ] || [ "\$BUILD_SCOPE" = "STUDENT_COURSE" ]; then
    deploy_system "学生选课系统" "${SERVER_DEPLOY_DIR}/paike/student-course" "student-docker-compose.override.yml"
fi

if [ "\$BUILD_SCOPE" = "ALL" ] || [ "\$BUILD_SCOPE" = "TEACHER_SYSTEM" ]; then
    deploy_system "教师排课系统" "${SERVER_DEPLOY_DIR}/paike/teacher-scheduling-system" "teacher-docker-compose.override.yml"
fi

# 清理未使用的镜像
echo ""
echo "🧹 清理未使用的镜像..."
docker image prune -f

echo ""
echo "=========================================="
echo "✅ 部署完成！"
echo "=========================================="
echo "学生选课PC前端: http://${SSH_HOST}:45083"
echo "学生选课移动端: http://${SSH_HOST}:45084"
echo "学生选课系统后端: http://${SSH_HOST}:45081/api"
echo "教师排课系统前端: http://${SSH_HOST}:45100"
echo "教师排课系统后端: http://${SSH_HOST}:45082/api"
echo "教室管理系统: http://${SSH_HOST}:45103"
echo "镜像标签: ${IMAGE_TAG}"
echo "=========================================="
"""

                    // 生成各系统的 override 文件
                    if (params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'STUDENT_COURSE') {
                        writeFile file: 'student-override.yml', text: """services:
  student-backend:
    image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-backend:${IMAGE_TAG}
  student-frontend:
    image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-frontend:${IMAGE_TAG}
  student-mobile:
    image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/student-course-mobile:${IMAGE_TAG}
"""
                        bat """
                            "${SCP_BIN}" -i "${SSH_KEY}" -P ${SSH_PORT} -o StrictHostKeyChecking=no student-override.yml ${SSH_USER}@${SSH_HOST}:/tmp/student-docker-compose.override.yml
                        """
                    }

                    if (params.BUILD_SCOPE == 'ALL' || params.BUILD_SCOPE == 'TEACHER_SYSTEM') {
                        writeFile file: 'teacher-override.yml', text: """services:
  teacher-backend:
    image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-backend:${IMAGE_TAG}
  teacher-frontend:
    image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/teacher-scheduling-frontend:${IMAGE_TAG}
"""
                        bat """
                            "${SCP_BIN}" -i "${SSH_KEY}" -P ${SSH_PORT} -o StrictHostKeyChecking=no teacher-override.yml ${SSH_USER}@${SSH_HOST}:/tmp/teacher-docker-compose.override.yml
                        """
                    }

                    // 上传并执行部署脚本
                    bat """
                        "${SCP_BIN}" -i "${SSH_KEY}" -P ${SSH_PORT} -o StrictHostKeyChecking=no deploy-all.sh ${SSH_USER}@${SSH_HOST}:/tmp/deploy-all.sh
                        "${SSH_BIN}" -i "${SSH_KEY}" -p ${SSH_PORT} -o StrictHostKeyChecking=no ${SSH_USER}@${SSH_HOST} "chmod +x /tmp/deploy-all.sh && bash /tmp/deploy-all.sh"
                    """
                }
            }
        }
    }

    post {
        success {
            script {
                def deployMsg = params.SKIP_DEPLOY ? "（仅构建，未部署）" : ""
                echo """
========================================
✅ 构建成功！${deployMsg}
========================================
分支: ${GIT_BRANCH}
构建号: ${env.BUILD_NUMBER}
镜像标签: ${IMAGE_TAG}
构建范围: ${params.BUILD_SCOPE}
学生选课PC前端: http://${SSH_HOST}:45083
学生选课移动端: http://${SSH_HOST}:45084
学生选课系统后端: http://${SSH_HOST}:45081/api
教师排课系统前端: http://${SSH_HOST}:45100
教师排课系统后端: http://${SSH_HOST}:45082/api
教室管理系统: http://${SSH_HOST}:45103
========================================
"""
            }
        }

        failure {
            echo """
========================================
❌ 构建失败！
========================================
分支: ${GIT_BRANCH}
构建号: ${env.BUILD_NUMBER}
请查看 Jenkins 控制台日志排查问题
========================================
"""
        }

        always {
            echo "==== 清理工作空间临时文件 ===="
            bat """
                if exist deploy-all.sh del deploy-all.sh
                if exist student-override.yml del student-override.yml
                if exist teacher-override.yml del teacher-override.yml
            """
        }
    }
}
