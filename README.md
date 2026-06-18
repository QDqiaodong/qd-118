# 厂区弱电布线配件资产台账系统

> 工厂弱电库房专用资产登记管理系统，管控接线端子、线槽、固定卡扣等配件，实现基础建档、领用、报废全流程管理。

---

## 📋 项目简介

本系统是针对工厂弱电库房设计的专用资产登记管理系统，主要用于管理接线端子、线槽、固定卡扣等弱电布线配件的完整生命周期。系统采用前后端分离架构，支持配件档案录入、车间领用出库、半年度库房清点、老化配件报废归档等核心业务功能。

---

## 🚀 快速开始

### 一键部署

```bash
./deploy.sh
```

### 手动部署

```bash
docker compose up -d --build
```

部署成功后，终端将自动打印访问地址。

---

## 🌐 访问地址

| 服务 | 地址 |
|------|------|
| **前端页面** | http://localhost:3028 |
| **前端页面** | http://127.0.0.1:3028 |
| **后端 API** | http://127.0.0.1:8108/api |
| **MySQL** | 127.0.0.1:3318 |
| **Redis** | 127.0.0.1:6398 |

---

## 📦 端口分配表

本项目端口已固定分配，禁止与其他项目复用：

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3028 | Nginx 静态资源服务 |
| 后端 | 8108 | SpringBoot API 服务 |
| MySQL | 3318 | 数据库服务 |
| Redis | 6398 | 缓存服务 |

> **注意**：所有端口仅绑定 `127.0.0.1`，不对外网暴露。如端口被占用，部署脚本将提示占用进程信息，建议修改 `.env` 文件重新分配端口。

---

## 🏗️ 技术栈

### 前端
- **框架**: Vue 3.4 + Vite 5
- **UI 组件**: Element Plus 2.4
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP 客户端**: Axios 1.6
- **样式**: SCSS

### 后端
- **框架**: Spring Boot 3.3
- **JDK**: OpenJDK 17
- **ORM**: Spring Data JPA
- **缓存**: Spring Data Redis
- **数据库**: MySQL 8.0
- **构建工具**: Maven 3.9
- **参数校验**: Jakarta Validation

### 基础设施
- **反向代理**: Nginx Alpine
- **容器化**: Docker + Docker Compose
- **缓存中间件**: Redis 7 Alpine
- **数据库**: MySQL 8.0

---

## ✨ 核心功能模块

### 1. 布线配件档案录入
- 录入配件型号、规格、入库数量、库房分区
- 树形分类选择（接线端子、线槽、固定卡扣三大类）
- 数值输入区间校验（数量 0-99999）
- 库房分区管理（A/B/C/D 区）

### 2. 车间领用出库登记
- 登记领用车间与配件领用数量
- 实时显示当前库存
- 自动校验领用数量不超过库存
- 提交后自动扣减库存

### 3. 半年度库房清点
- 批量录入所有配件实物数量
- 自动计算库存差异（盘盈/盘亏/账实相符）
- 一键提交清点记录并更新库存
- 差异状态标签可视化展示

### 4. 老化配件报废归档
- 登记过期破损配件报废数量
- 6 种报废原因分类
- 自动校验报废数量不超过库存
- 提交后自动扣减库存并归档

### 5. 数据统计仪表盘
- 配件种类总数统计
- 库存总数量统计
- 本月领用数量统计
- 本月报废数量统计
- 库存 Top 5 配件柱状图

---

## 🗂️ 项目结构

```
qd-118/
├── .env                      # 全局环境变量配置
├── .dockerignore             # 全局 Docker 忽略文件
├── docker-compose.yml        # Docker Compose 编排文件
├── deploy.sh                 # 一键部署脚本
├── README.md                 # 项目说明文档
├── backend/                  # 后端项目
│   ├── .dockerignore         # 后端 Docker 忽略文件
│   ├── Dockerfile            # 后端 Docker 构建文件
│   ├── pom.xml               # Maven 依赖配置
│   ├── settings.xml          # Maven 镜像源配置
│   ├── sql/
│   │   └── init.sql          # 数据库初始化脚本
│   └── src/main/
│       ├── java/com/weakcurrent/
│       │   ├── common/       # 通用类（返回、异常处理）
│       │   ├── config/       # 配置类（Redis、CORS、Jackson）
│       │   ├── controller/   # 控制层
│       │   ├── dto/          # 数据传输对象
│       │   ├── entity/       # 实体类
│       │   ├── repository/   # 数据访问层
│       │   ├── service/      # 业务逻辑层
│       │   └── WeakCurrentApplication.java
│       └── resources/
│           └── application.yml
└── frontend/                 # 前端项目
    ├── .dockerignore         # 前端 Docker 忽略文件
    ├── .npmrc                # NPM 镜像源配置
    ├── Dockerfile            # 前端 Docker 构建文件
    ├── nginx.conf            # Nginx 配置
    ├── package.json          # NPM 依赖配置
    ├── package-lock.json     # NPM 依赖锁定
    ├── vite.config.js        # Vite 配置
    ├── index.html            # HTML 入口
    └── src/
        ├── api/              # API 接口封装
        ├── components/       # 公共组件
        ├── router/           # 路由配置
        ├── styles/           # 全局样式
        ├── utils/            # 工具函数
        ├── views/            # 页面组件
        ├── App.vue           # 根组件
        └── main.js           # 应用入口
```

---

## 🐳 Docker 构建优化

### 分层缓存机制
前端和后端均采用多阶段构建 + 分层缓存策略：

**后端构建流程：**
1. 复制 `settings.xml` 配置 Maven 镜像
2. 复制 `pom.xml` 并执行 `mvn dependency:go-offline` 下载依赖
3. 复制 `src` 源码并执行 `mvn clean package` 编译
4. 仅复制 jar 包到 JRE 运行镜像

**前端构建流程：**
1. 配置 NPM 国内镜像源
2. 复制 `package.json` + `package-lock.json` 并执行 `npm ci` 安装依赖
3. 复制源码并执行 `npm run build` 构建
4. 仅复制 dist 目录到 Nginx 镜像

> **效果**：仅修改业务代码时，不会重新下载依赖，构建速度提升 80% 以上。

### 国内镜像加速
- **Maven 镜像**: 华为云 `https://repo.huaweicloud.com/repository/maven/`
- **NPM 镜像**: 淘宝 npmmirror `https://registry.npmmirror.com/`
- **Docker 镜像**: DaoCloud `docker.m.daocloud.io`

### 基础镜像统一前缀
所有基础镜像通过 `${DOCKER_REGISTRY}` 环境变量统一前缀，避免 DockerHub 直连失败问题。

### MySQL 内存限制
MySQL 容器内存限制为 512MB，预留内存 256MB，避免资源耗尽。

---

## 🔧 配置说明

### .env 环境变量

```dotenv
DOCKER_REGISTRY=docker.m.daocloud.io

PROJECT_NAME=weakcurrent
COMPOSE_PROJECT_NAME=qd-118

# 端口配置（固定，请勿随意修改）
FRONTEND_PORT=3028
BACKEND_PORT=8108
MYSQL_PORT=3318
REDIS_PORT=6398
SERVER_PORT=8108

# 数据库配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=weakcurrent_db
MYSQL_USER=weakcurrent
MYSQL_PASSWORD=weakcurrent123

# Redis 配置
REDIS_PASSWORD=redis123456
```

### 端口冲突处理

如遇到端口冲突，部署脚本会自动检测并提示占用进程：

```bash
❌ 端口 3028 已被占用! (PID: 12345, 进程: node)
请先停止占用端口的进程，或修改 .env 文件中的端口配置。
```

**解决方法**：
1. 停止占用进程：`kill -9 <PID>`
2. 或修改 `.env` 文件中的端口号

---

## ✅ 验证检查清单

部署完成后，请执行以下验证：

### 1. 端口监听验证
```bash
lsof -nP -iTCP:3028 -sTCP:LISTEN
lsof -nP -iTCP:8108 -sTCP:LISTEN
```

### 2. 前端访问验证
```bash
# 两个地址必须返回相同内容
curl -sS http://127.0.0.1:3028 | head -20
curl -sS http://localhost:3028 | head -20
```

### 3. 后端 API 验证
```bash
curl -s http://127.0.0.1:8108/api/dashboard/stats
curl -s http://127.0.0.1:8108/api/categories/tree
```

### 4. 浏览器验证
打开浏览器访问：
- ✅ http://localhost:3028
- ✅ http://127.0.0.1:3028

确认两个地址打开的是同一项目，页面标题为"弱电布线配件库房管理系统"。

---

## 📝 常用命令

```bash
# 启动服务
docker compose up -d --build

# 停止服务
docker compose down

# 停止服务并清理数据卷（慎用！）
docker compose down -v

# 重启服务
docker compose restart

# 查看日志
docker compose logs -f

# 查看指定服务日志
docker compose logs -f frontend
docker compose logs -f backend

# 查看容器状态
docker compose ps

# 进入容器
docker compose exec backend sh
docker compose exec mysql bash
```

---

## 🔒 安全注意事项

1. 所有端口仅绑定 `127.0.0.1`，禁止对外暴露
2. 生产环境请修改默认密码
3. 数据库连接使用容器内部网络，不对外映射
4. Redis 已配置密码认证
5. CORS 配置允许跨域请求（仅开发环境）

---

## 🗄️ 数据库 ER 图

```
accessory_category (配件分类)
    ├── id (PK)
    ├── name
    ├── parent_id
    └── sort

accessory (配件档案)
    ├── id (PK)
    ├── name
    ├── model
    ├── spec
    ├── category_id (FK)
    ├── category_name
    ├── stock_quantity
    ├── warehouse_zone
    └── unit

stock_out (领用出库)
    ├── id (PK)
    ├── accessory_id (FK)
    ├── workshop
    ├── quantity
    ├── operator
    └── out_time

inventory_check (库房清点)
    ├── id (PK)
    ├── accessory_id (FK)
    ├── physical_quantity
    ├── system_quantity
    ├── difference
    ├── check_person
    └── check_time

scrap_record (报废归档)
    ├── id (PK)
    ├── accessory_id (FK)
    ├── quantity
    ├── reason
    ├── operator
    └── scrap_time
```

---

## 📄 许可证

Copyright © 2026 厂区弱电布线配件资产台账系统

---

## 🤝 技术支持

如遇问题，请检查：
1. Docker 和 Docker Compose 是否已正确安装
2. `.env` 文件中的端口是否被占用
3. 镜像仓库地址是否可访问
4. 查看容器日志排查具体错误
