# ===========================================
# Docker 部署中文防乱码检查清单
# 部署前请逐项确认
# ===========================================

## ✅ 1. 数据库层 (MySQL)
- [x] 字符集: `utf8mb4`
- [x] 排序规则: `utf8mb4_unicode_ci`
- [x] JDBC URL: `useUnicode=true&characterEncoding=utf-8`
- [x] 初始化 SQL: `SET NAMES utf8mb4`

**配置位置**: 
- `docker-compose.yml` → mysql.environment
- `application-dev.yml` → spring.datasource.url

## ✅ 2. JVM 层 (Spring Boot)
- [x] JVM 参数: `-Dfile.encoding=UTF-8`
- [x] JVM 参数: `-Dsun.jnu.encoding=UTF-8`
- [x] JVM 参数: `-Dsun.stdout.encoding=UTF-8`
- [x] JVM 参数: `-Dsun.stderr.encoding=UTF-8`
- [x] Servlet 编码: `server.servlet.encoding.charset=UTF-8`
- [x] 强制编码: `force-response: true`
- [x] 消息转换器: `StringHttpMessageConverter(UTF-8)`
- [x] Jackson 默认编码: UTF-8

**配置位置**: 
- `Dockerfile` → ENV JAVA_OPTS
- `application.yml` → server.servlet.encoding
- `WebMvcConfig.java` → configureMessageConverters()

## ✅ 3. HTTP 响应层
- [x] Content-Type: `application/json; charset=utf-8`
- [x] Accept: `application/json; charset=utf-8`
- [x] Nginx charset: `utf-8`
- [x] Nginx source_charset: `utf-8`
- [x] Nginx charset_types: 完整列表

**配置位置**: 
- `nginx.conf` → charset settings
- `request.ts` → axios headers

## ✅ 4. 前端层 (Vue)
- [x] HTML meta: `<meta charset="UTF-8">`
- [x] HTML lang: `lang="zh-CN"`
- [x] 中文字体栈: PingFang SC, Microsoft YaHei
- [x] 请求头: `charset=utf-8`

**配置位置**: 
- `frontend/index.html`
- `frontend/src/api/request.ts`

## ✅ 5. 容器层 (Docker)
- [x] 基础镜像: Alpine (支持 UTF-8)
- [x] 中文字体: `fontconfig liberation-fonts`
- [x] 时区: `Asia/Shanghai`
- [x] 环境变量: 所有编码参数传递
- [x] 资源限制: CPU/Memory limits

**配置位置**: 
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker-compose.yml` → deploy.resources

## ✅ 6. 文件系统层
- [ ] 源代码文件编码: **UTF-8 with BOM 或 UTF-8**
- [ ] IDE 设置: Project Encoding = UTF-8
- [ ] Git 配置: `core.autocrlf=input`, `core.quotepath=false`

**验证命令**:
```bash
# 检查文件编码
file backend/src/main/java/com/library/common/Result.java

# 查看文件是否包含 BOM
xxd frontend/index.html | head -1
```

## 🚀 部署验证步骤

### 1. 构建镜像
```bash
cd backend
docker compose build --no-cache
```

### 2. 启动服务
```bash
cp .env.example .env
# 编辑 .env 填入实际密码
docker compose up -d
```

### 3. 等待健康检查通过
```bash
watch -n 2 docker ps
# 确认所有容器状态为 healthy
```

### 4. 测试中文接口
```bash
# 登录测试 (应该返回 "操作成功" 而非乱码)
curl -s http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"username":"admin","password":"admin123"}' | jq .

# 统计数据测试
curl -s http://localhost:8080/api/statistics/borrow-trend \
  -H "Authorization: Bearer YOUR_TOKEN" | jq .
```

### 5. 前端页面验证
```bash
# 访问前端页面，检查:
# - 导航菜单中文显示正常
# - 图表标签中文正常
# - 表格数据中文正常
# - 提示消息中文正常
open http://localhost:80
```

## ⚠️ 常见问题排查

### 问题1: API 返回乱码
**症状**: `"message": "æä½œæˆåŠŸ"`  
**原因**: HTTP 响应未声明 UTF-8  
**解决**: 已在 WebMvcConfig 中强制设置

### 问题2: 数据库存储乱码
**症状**: 数据库中显示 ???  
**原因**: MySQL 字符集不是 utf8mb4  
**解决**: 
```sql
ALTER DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE table_name CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 问题3: 前端页面方块字/问号
**症状**: 中文显示为 □□□ 或 ???  
**原因**: 容器缺少中文字体  
**解决**: Dockerfile 中已安装 liberation-fonts

### 问题4: 日志乱码
**症状**: 控制台日志中文乱码  
**原因**: JVM 未指定 UTF-8  
**解决**: JAVA_OPTS 中已添加所有编码参数

## 📊 验证结果记录

| 检查项 | 状态 | 备注 |
|--------|------|------|
| MySQL 字符集 | ✅ | utf8mb4 |
| JVM 编码参数 | ✅ | 4个-D参数 |
| HTTP 响应编码 | ✅ | 消息转换器已配置 |
| Nginx 编码 | ✅ | charset utf-8 |
| 前端 HTML 编码 | ✅ | UTF-8 meta |
| 容器字体支持 | ✅ | liberation-fonts |
| 接口返回中文 | ⏳ | 需重启后端验证 |
| 页面显示中文 | ⏳ | 需浏览器验证 |

---

**最后更新**: 2026-05-18  
**检查人**: AI Assistant  
**部署版本**: v2.0.0
