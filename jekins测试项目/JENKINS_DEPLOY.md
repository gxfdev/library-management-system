# Jenkins CI/CD 部署指南

## 前置条件

1. **Jenkins 安装** (推荐 Docker 方式)
```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --group-add docker \
  jenkins/jenkins:lts
```

2. **Jenkins 插件安装**
   - Pipeline
   - Docker Pipeline
   - Git
   - Slack Notification (可选)
   - AnsiColor (可选)

3. **凭证配置** (`Manage Jenkins` → `Credentials`)
   - `docker-registry`: Docker Hub 或私有仓库凭证
   - `git-credentials`: Git 仓库访问凭证

## 项目配置步骤

### 1. 创建 Pipeline 任务

1. 新建 Item → 选择 "Pipeline"
2. 配置:
   - **Pipeline**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: 你的 Git 仓库地址
   - **Credentials**: 选择 git-credentials
   - **Branch**: `*/main` (或 master)
   - **Script Path**: `Jenkinsfile`

### 2. 触发器配置

建议配置:
- **GitHub hook trigger for GITScm polling**: 启用自动触发
- **Poll SCM**: `H/5 * * * *` (每5分钟检查)

### 3. 环境变量

在 `.env` 文件中配置生产环境变量:

```bash
# 复制模板
cp backend/.env.example .env
# 编辑实际值
vim .env
```

## 部署流程

```
代码提交 → 触发构建 → 单元测试 → 安全扫描 → 构建镜像 → 推送镜像 → 部署
     ↓           ↓          ↓         ↓          ↓        ↓       ↓
   GitHub    Jenkins    JUnit5    OWASP      Docker   Registry  Docker Compose
```

### 分支策略

| 分支 | 环境 | 操作 |
|------|------|------|
| `develop` | Staging | 自动部署 |
| `main/master` | Production | 手动确认后部署 |

## 常用命令

```bash
# 手动触发构建
curl -X POST http://localhost:8080/job/library-management/build

# 查看构建日志
curl http://localhost:8080/job/library-management/lastBuild/consoleText

# 本地测试 Docker 部署
cd backend
docker compose up -d --build

# 停止所有服务
docker compose down -v

# 清理未使用的资源
docker system prune -a
```

## 故障排查

### 构建失败
1. 检查控制台输出: `Console Output`
2. 确认 Maven/Node.js 版本兼容性
3. 检查依赖是否可下载

### Docker 推送失败
1. 确认 registry 凭证正确
2. 检查网络连接和防火墙
3. 确认镜像名称格式正确

### 部署后乱码
1. 确认 MySQL 使用 utf8mb4 字符集
2. 检查 JVM 编码参数: `-Dfile.encoding=UTF-8`
3. 确认 Nginx 配置: `charset utf-8`
4. 确认容器包含中文字体包

## 监控与告警

### 健康检查端点
- 后端: `http://localhost:8080/api/actuator/health`
- 前端: `http://localhost:80` (HTTP 200)

### 日志查看
```bash
# 查看后端日志
docker logs library-backend -f --tail=100

# 查看 MySQL 日志
docker logs library-mysql -f --tail=50

# 查看 Nginx 日志
docker logs library-frontend -f --tail=50
```
