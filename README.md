<p align="center">
  <img src="docs/images/logo.png" alt="Library Management System" width="120" height="120">
</p>

<h1 align="center">📚 智慧图书馆管理系统</h1>
<h3 align="center">Library Management System - 现代化图书管理解决方案</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=java&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot 3.x">
  <img src="https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white" alt="Vue 3">
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL 8">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License: MIT">
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#截图预览">截图预览</a> •
  <a href="#API文档">API文档</a> •
  <a href="#贡献指南">贡献指南</a> •
  <a href="#许可证">许可证</a>
</p>

---

## 🌟 项目简介

**智慧图书馆管理系统** 是一个基于前后端分离架构的现代化图书管理平台，采用 **Spring Boot 3 + Vue 3 + TypeScript** 技术栈开发。系统提供完整的图书馆业务流程管理，包括图书采购、编目、借阅归还、读者管理、统计报表等核心功能，适用于高校图书馆、公共图书馆及企业内部图书馆的数字化管理需求。

### ✨ 核心亮点

- 🔐 **安全可靠**：JWT 身份认证 + RBAC 角色权限控制 + 图形验证码防护
- 🎨 **现代界面**：Vue 3 Composition API + Element Plus + 响应式设计
- ⚡ **高性能**：MyBatis-Plus ORM + Redis 缓存 + 分页查询优化
- 📊 **数据可视化**：ECharts 图表展示借阅趋势与分类分布
- 🛡️ **安全加固**：XSS/SQL 注入防护 + CORS 配置 + 安全响应头
- 🐳 **容器化部署**：Docker Compose 一键启动 + Nginx 反向代理

---

## 📋 功能特性

| 模块 | 功能描述 |
|------|----------|
| 🔑 **身份认证** | 用户登录/注册、图形验证码、JWT Token 认证、密码重置 |
| 👥 **用户管理** | 读者/管理员 CRUD、角色分配、状态控制、批量操作 |
| 📚 **图书管理** | 图书增删改查、ISBN 自动识别、分类管理、出版社管理 |
| 🏷️ **分类管理** | 多级分类体系、分类树形展示、分类统计 |
| 📖 **图书借阅** | 借书/还书流程、续借操作、逾期检测、罚款计算 |
| 📋 **我的借阅** | 借阅记录查看、借阅状态跟踪、历史记录查询 |
| 📊 **数据看板** | 实时统计数据、借阅趋势图表、热门图书排行、分类占比 |
| 📈 **统计报表** | 借阅统计分析、库存报表、用户活跃度报告 |
| 🗄️ **库存管理** | 库存盘点、图书上架下架、库存预警 |
| 📦 **采购管理** | 采购申请审批、供应商管理、采购订单追踪 |
| 🏢 **部门管理** | 部门组织架构、部门成员管理 |
| 📢 **公告通知** | 系统公告发布、消息推送、通知管理 |
| 👤 **个人中心** | 头像上传、个人信息修改、实名认证、密码修改 |

### 🎯 角色权限

| 角色 | 权限范围 |
|------|----------|
| **超级管理员 (ADMIN)** | 全部功能权限，系统配置管理 |
| **图书管理员 (LIBRARIAN)** | 图书管理、借阅管理、库存管理、统计报表 |
| **普通读者 (READER)** | 图书检索、借阅还书、个人中心、借阅记录 |

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 Frontend                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Vue 3    │  │ Vite     │  │ Element  │  │ Pinia    │    │
│  │ TS 5.x   │  │ Build    │  │ Plus     │  │ Store    │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
├─────────────────────────────────────────────────────────────┤
│                        后端 Backend                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Spring   │  │ Spring   │  │ MyBatis  │  │ Spring   │    │
│  │ Boot 3.x │  │ Security │  │ Plus     │  │ Data JPA │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
├─────────────────────────────────────────────────────────────┤
│                        数据层 Data                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ MySQL    │  │ Redis    │  │ Local FS │                  │
│  │ 8.x      │  │ Cache    │  │ Uploads  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈详情

#### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 LTS | 编程语言 |
| Spring Boot | 3.2.x | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.12.x | Token 认证 |
| Swagger/OpenAPI | 2.x | API 文档 |
| Lombok | 1.18.x | 代码简化 |
| H2 Database | 2.x | 单元测试数据库 |

#### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.4+ | 渐进式框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |
| SCSS | - | CSS 预处理器 |
| ECharts | 5.x | 数据可视化 |

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Node.js**: 18 或更高版本
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.8 或更高版本
- **pnpm**: 8 或更高版本（推荐）

### 1️⃣ 克隆项目

```bash
git clone https://github.com/gxfdev/library-management-system.git
cd library-management-system
```

### 2️⃣ 后端配置

```bash
# 进入后端目录
cd backend

# 创建环境变量文件
cp .env.example .env

# 编辑 .env 文件，配置数据库连接等信息
# .env 主要配置项:
#   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/library_db
#   SPRING_DATASOURCE_USERNAME=root
#   SPRING_DATASOURCE_PASSWORD=your_password
#   JWT_SECRET=your-jwt-secret-key

# 导入数据库初始化脚本
mysql -u root -p < sql/init.sql

# 启动后端服务
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 3️⃣ 前端配置

```bash
# 进入前端目录（新终端）
cd frontend

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

前端开发服务器默认运行在 `http://localhost:5173`

### 4️⃣ 访问系统

打开浏览器访问：`http://localhost:5173`

#### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 图书管理员 | librarian | librarian123 |
| 普通读者 | reader | reader123 |

### 🐳 Docker 快速部署（可选）

```bash
# 使用 Docker Compose 一键启动
docker-compose up -d

# 服务将自动运行在以下端口:
# - 前端: http://localhost:80
# - 后端 API: http://localhost:8080
# - MySQL: localhost:3306
```

---

## 📁 项目结构

```
library-management-system/
├── backend/                          # 后端项目
│   ├── src/main/java/com/library/
│   │   ├── LibraryApplication.java   # 启动类
│   │   ├── common/                   # 公共类（Result, PageResult）
│   │   ├── config/                   # 配置类
│   │   │   ├── SecurityConfig.java   # 安全配置
│   │   │   ├── CorsConfig.java       # 跨域配置
│   │   │   ├── SwaggerConfig.java    # Swagger配置
│   │   │   └── XssSqlFilter.java    # 安全过滤器
│   │   ├── controller/               # 控制器层
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── service/                  # 业务逻辑层
│   │   ├── security/                 # 安全模块（JWT）
│   │   └── task/                     # 定时任务
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置文件
│   │   ├── application-dev.yml       # 开发环境配置
│   │   ├── application-prod.yml      # 生产环境配置
│   │   └── mapper/                   # MyBatis XML 映射
│   ├── sql/                          # 数据库脚本
│   ├── pom.xml                       # Maven 配置
│   └── Dockerfile                    # Docker 构建文件
│
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── api/                      # API 接口定义
│   │   ├── assets/                   # 静态资源
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # Pinia 状态管理
│   │   ├── types/                    # TypeScript 类型定义
│   │   ├── views/                    # 页面组件
│   │   ├── App.vue                   # 根组件
│   │   └── main.ts                   # 入口文件
│   ├── index.html                    # HTML 模板
│   ├── package.json                  # 依赖配置
│   ├── vite.config.ts                # Vite 配置
│   └── tsconfig.json                 # TypeScript 配置
│
├── docs/                             # 项目文档
│   └── images/                       # 截图图片
├── docker-compose.yml                # Docker 编排文件
├── nginx.conf                        # Nginx 配置
├── .gitignore                        # Git 忽略规则
├── LICENSE                           # MIT 许可证
└── README.md                         # 项目说明文档
```

---

## 📸 截图预览

<!-- TODO: 替换为实际截图路径 -->

### 登录页面
<p align="center">
  <img src="docs/images/login.png" alt="登录页面" width="800">
</p>
<p align="center">支持图形验证码的安全登录界面</p>

### 数据仪表盘
<p align="center">
  <img src="docs/images/dashboard.png" alt="数据仪表盘" width="800">
</p>
<p align="center">实时数据统计、借阅趋势图表、分类分布可视化</p>

### 图书管理
<p align="center">
  <img src="docs/images/book-management.png" alt="图书管理" width="800">
</p>
<p align="center">完整的图书 CRUD 操作、高级搜索筛选、批量导入导出</p>

### 个人中心
<p align="center">
  <img src="docs/images/profile.png" alt="个人中心" width="800">
</p>
<p align="center">个人信息编辑、头像上传、实名认证、密码修改</p>

### 用户管理
<p align="center">
  <img src="docs/images/user-management.png" alt="用户管理" width="800">
</p>
<p align="center">多角色用户管理、权限分配、状态控制</p>

---

## 📡 API 文档

系统集成了 **Swagger/OpenAPI** 文档，启动后端服务后访问：

```
http://localhost:8080/swagger-ui.html
```

主要 API 模块：

| 模块 | 基础路径 | 说明 |
|------|----------|------|
| 认证模块 | `/api/auth` | 登录、注册、Token 刷新 |
| 用户模块 | `/api/users` | 用户 CRUD、角色管理 |
| 图书模块 | `/api/books` | 图书管理、搜索 |
| 借阅模块 | `/api/borrows` | 借阅、归还、续借 |
| 分类模块 | `/api/categories` | 分类管理 |
| 统计模块 | `/api/statistics` | 数据统计报表 |
| 个人中心 | `/api/profile` | 个人信息修改 |
| 文件上传 | `/api/uploads` | 头像、封面上传 |

---

## 🔒 安全特性

- ✅ **JWT Token 认证**：无状态身份验证，Token 过期机制
- ✅ **RBAC 角色权限**：细粒度的接口级权限控制
- ✅ **图形验证码**：登录防暴力破解保护
- ✅ **密码加密存储**：BCrypt 加密算法
- ✅ **XSS 防护**：输入过滤与输出转义
- ✅ **SQL 注入防护**：参数化查询 + MyBatis 安全过滤
- ✅ **CORS 跨域配置**：安全的跨域资源共享策略
- ✅ **安全响应头**：X-Frame-Options、Content-Security-Policy 等
- ✅ **登录限流**：防止暴力破解的频率限制
- ✅ **操作日志**：关键操作的审计日志记录

---

## 🤝 贡献指南

我们欢迎任何形式的贡献！无论是 Bug 修复、新功能开发还是文档改进。

### 如何贡献

1. **Fork 本仓库**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **提交 Pull Request**

### 代码规范

- 后端代码遵循阿里巴巴 Java 开发规范
- 前端代码遵循 Vue 3 官方风格指南
- 提交信息使用 Conventional Commits 规范
- 确保 PR 通过所有 CI 检查

### 问题反馈

如果您发现 Bug 或有功能建议，请通过 [GitHub Issues](https://github.com/gxfdev/library-management-system/issues) 提交：

- 🐛 **Bug 报告**：请详细描述复现步骤和环境信息
- 💡 **功能建议**：请说明使用场景和预期效果
- 📝 **文档问题**：欢迎指出文档中的错误或不清晰之处

---

## 📊 项目路线图

- [x] 基础图书管理功能
- [x] 用户认证与权限系统
- [x] 借阅归还流程
- [x] 数据统计仪表盘
- [x] 图形验证码安全机制
- [ ] 移动端适配 / PWA 支持
- [ ] 图书推荐算法（协同过滤）
- [ ] 微信小程序端
- [ ] 多语言国际化 (i18n)
- [ ] 消息通知系统（邮件/短信）
- [ ] 数据导出（Excel/PDF）
- [ ] 高级报表生成器

---

## 🙏 致谢

感谢以下开源项目为本项目提供的技术基础：

- [Spring Boot](https://spring.io/projects/spring-boot) - 企业级 Java 应用框架
- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Element Plus](https://element-plus.org/) - 基于 Vue 3 的组件库
- [MyBatis-Plus](https://baomidou.com/) - 强大的 MyBatis 增强工具
- [Apache ECharts](https://echarts.apache.org/) - 数据可视化图表库

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

```
MIT License

Copyright (c) 2026 gxfdev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📮 联系方式

- **作者**: gxfdev
- **GitHub**: [https://github.com/gxfdev](https://github.com/gxfdev)
- **邮箱**: gxfdev@example.com
- **项目地址**: [https://github.com/gxfdev/library-management-system](https://github.com/gxfdev/library-management-system)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star 支持一下！**

Made with ❤️ by [gxfdev](https://github.com/gxfdev)

</div>
