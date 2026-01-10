# Docker 部署指南

本文档介绍如何使用 Docker 和 Docker Compose 部署拼车和交通监控系统。

## 目录结构

```
carpool/
├── docker-compose.yml          # Docker Compose 配置文件
├── .env.example               # 环境变量示例文件
├── script/
│   ├── init.sql               # 数据库初始化脚本
│   ├── Dockerfile             # 数据采集服务 Dockerfile
│   ├── requirements.txt       # Python 依赖
│   ├── config.json            # 数据采集配置
│   ├── run.py                 # 原始采集脚本
│   └── run_docker.py          # Docker 版本采集脚本
├── carpool-b/                 # 后端 Spring Boot 应用
│   ├── Dockerfile             # 后端 Dockerfile
│   ├── .dockerignore          # 后端构建忽略文件
│   └── src/main/resources/
│       └── application-docker.properties  # Docker 环境配置
└── carpool-f/                 # 前端 Vue 应用
    ├── Dockerfile             # 前端 Dockerfile
    ├── .dockerignore          # 前端构建忽略文件
    └── nginx.conf             # Nginx 配置文件
```

## 前置要求

1. **Docker**: 安装 Docker Engine 20.10+
   - [Linux 安装指南](https://docs.docker.com/engine/install/)
   - [Windows/Mac 安装 Docker Desktop](https://www.docker.com/products/docker-desktop/)

2. **Docker Compose**: 安装 Docker Compose v2.0+
   - 通常随 Docker Desktop 自动安装
   - Linux: `sudo apt-get install docker-compose-plugin`

3. **端口要求**: 确保以下端口未被占用
   - `80`: 前端 Web 服务
   - `8080`: 后端 API 服务
   - `3306`: MySQL 数据库

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repository-url>
cd carpool
```

### 2. 配置数据采集服务

**重要**: 数据采集服务需要百度地图 API Key。编辑 `script/config.json` 文件：

```bash
# 编辑配置文件
nano script/config.json
```

将 `YOUR_BAIDU_API_KEY_HERE` 替换为您的百度地图 API Key：

```json
{
  "baidu_api": {
    "ak": "YOUR_ACTUAL_BAIDU_API_KEY"
  }
}
```

获取 API Key:
1. 访问 [百度地图开放平台](https://lbsyun.baidu.com/)
2. 注册/登录账号
3. 创建应用并申请"路况交通"服务权限
4. 复制您的 AK (API Key)

### 3. 配置环境变量（可选）

如需自定义配置，可以复制 `.env.example` 为 `.env` 并修改：

```bash
cp .env.example .env
nano .env
```

或者直接编辑 `docker-compose.yml` 文件中的环境变量。

### 4. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

首次启动会自动：
- 创建并初始化 MySQL 数据库
- 构建前后端 Docker 镜像
- 启动所有服务容器

### 4. 验证部署

```bash
# 检查服务健康状态
docker-compose ps

# 应该看到所有服务状态为 "Up" 或 "healthy"

# 访问应用
# 前端: http://localhost
# 后端 API: http://localhost:8080/api
# 数据库: localhost:3306
```

测试 API：
```bash
# 测试后端健康检查
curl http://localhost:8080/api/traffic/stats

# 测试数据库连接
docker exec -it carpool-mysql mysql -ucarpool -pcarpool_password -e "SHOW TABLES;"
```

## 服务说明

### MySQL 数据库 (`carpool-mysql`)
- **镜像**: mysql:8.0
- **端口**: 3306
- **数据持久化**: Docker volume `mysql_data`
- **初始化**: 自动执行 `script/init.sql`
- **默认用户**:
  - root / root_password_114514
  - carpool / carpool_password

### 后端服务 (`carpool-backend`)
- **基础镜像**: eclipse-temurin:21-jre-alpine
- **构建**: Gradle 多阶段构建
- **端口**: 8080
- **日志持久化**: Docker volume `backend_logs`
- **健康检查**: `/api/actuator/health`
- **环境变量**:
  - `SPRING_PROFILES_ACTIVE`: docker
  - `SPRING_DATASOURCE_URL`: MySQL 连接 URL
  - `SPRING_DATASOURCE_USERNAME`: carpool
  - `SPRING_DATASOURCE_PASSWORD**: carpool_password

### 前端服务 (`carpool-frontend`)
- **基础镜像**: nginx:alpine
- **构建**: Node.js + Vite 多阶段构建
- **端口**: 80
- **Nginx 配置**: `carpool-f/nginx.conf`
- **代理**: `/api/*` → `http://backend:8080/api/*`

### 数据采集服务 (`carpool-collector`)
- **基础镜像**: python:3.11-slim
- **构建**: Python 虚拟环境
- **日志持久化**: Docker volume `collector_logs`
- **配置文件**: `script/config.json` (挂载为只读)
- **采集间隔**: 默认 5 分钟（可在 config.json 配置）
- **监控道路**: 默认 12 条上海道路（可在 config.json 添加）
- **健康检查**: 检查 Python 进程是否运行

**数据采集服务说明**:
- 自动从百度地图 API 获取路况数据
- 将数据保存到 MySQL 数据库
- 支持配置文件动态调整采集参数
- 日志输出到 `/app/logs/collector.log`
- 如需禁用，在 `docker-compose.yml` 中注释掉 collector 服务

## 常用命令

### 启动和停止

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose stop

# 重启服务
docker-compose restart

# 停止并删除容器
docker-compose down

# 停止并删除容器、网络、数据卷（⚠️ 会删除数据库数据）
docker-compose down -v
```

### 日志查看

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql
docker-compose logs collector  # 数据采集服务日志

# 实时跟踪日志
docker-compose logs -f backend
docker-compose logs -f collector  # 实时查看采集日志

# 查看最近 100 行日志
docker-compose logs --tail=100 backend
docker-compose logs --tail=100 collector
```

### 容器管理

```bash
# 进入后端容器
docker exec -it carpool-backend sh

# 进入前端容器
docker exec -it carpool-frontend sh

# 进入 MySQL 容器
docker exec -it carpool-mysql mysql -ucarpool -pcarpool_password carpool

# 进入数据采集容器
docker exec -it carpool-collector sh

# 查看采集服务日志文件
docker exec carpool-collector tail -f /app/logs/collector.log

# 查看容器资源使用
docker stats carpool-backend carpool-frontend carpool-mysql carpool-collector
```

### 重新构建

```bash
# 重新构建并启动特定服务
docker-compose up -d --build backend

# 重新构建所有服务
docker-compose up -d --build

# 强制重新构建（不使用缓存）
docker-compose build --no-cache
docker-compose up -d
```

## 生产环境部署

### 1. 使用环境变量文件

创建 `.env` 文件：

```env
# MySQL 配置
MYSQL_ROOT_PASSWORD=your_strong_root_password
MYSQL_DATABASE=carpool
MYSQL_USER=carpool
MYSQL_PASSWORD=your_strong_password

# 后端配置
BACKEND_PORT=8080
SPRING_DATASOURCE_PASSWORD=your_strong_password

# 前端配置
FRONTEND_PORT=80
```

修改 `docker-compose.yml` 使用环境变量：

```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}
  SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
```

启动：
```bash
docker-compose up -d
```

### 2. 配置反向代理（可选）

如果已有 Nginx 或其他反向代理，可以修改 `docker-compose.yml`：

```yaml
services:
  frontend:
    ports:
      - "127.0.0.1:8081:80"  # 仅监听本地

  backend:
    ports:
      - "127.0.0.1:8080:8080"  # 仅监听本地
```

### 3. 安全建议

- **修改默认密码**: 生产环境务必修改所有默认密码
- **使用 HTTPS**: 配置 SSL/TLS 证书
- **限制端口暴露**: 使用防火墙限制外部访问
- **定期备份**:
  ```bash
  # 备份数据库
  docker exec carpool-mysql mysqldump -ucarpool -pcarpool_password carpool > backup.sql

  # 恢复数据库
  docker exec -i carpool-mysql mysql -ucarpool -pcarpool_password carpool < backup.sql
  ```
- **更新镜像**: 定期更新基础镜像和依赖

### 4. 性能优化

- **资源限制**: 在 `docker-compose.yml` 中添加资源限制
  ```yaml
  services:
    backend:
      deploy:
        resources:
          limits:
            cpus: '2'
            memory: 2G
          reservations:
            cpus: '1'
            memory: 1G
  ```

- **数据库优化**:
  - 添加 MySQL 缓冲池配置
  - 定期清理历史数据
  - 创建必要的索引（见 `script/create_historical_indexes.sql`）

## 故障排查

### 服务无法启动

```bash
# 查看详细日志
docker-compose logs backend
docker-compose logs mysql

# 检查端口占用
netstat -tulpn | grep :3306
netstat -tulpn | grep :8080
netstat -tulpn | grep :80
```

### 数据库连接失败

```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 测试数据库连接
docker exec -it carpool-mysql mysql -ucarpool -pcarpool_password -e "SELECT 1;"

# 查看数据库日志
docker-compose logs mysql
```

### 前端无法访问后端

```bash
# 检查网络连接
docker exec carpool-frontend wget -O- http://backend:8080/api/traffic/stats

# 检查 Nginx 配置
docker exec carpool-frontend cat /etc/nginx/conf.d/default.conf
```

### 重新初始化数据库

```bash
# 停止服务并删除数据卷
docker-compose down -v

# 重新启动（会重新执行 init.sql）
docker-compose up -d
```

## 开发环境

### 本地开发模式

如需在本地开发而不使用 Docker：

**后端**:
```bash
cd carpool-b
./gradlew bootRun
```

**前端**:
```bash
cd carpool-f
npm install
npm run dev
```

**数据库**:
- 使用本地 MySQL 或 Docker MySQL 容器
- 修改 `application.properties` 中的数据库连接配置

### 混合模式（Docker MySQL + 本地应用）

仅启动数据库：
```bash
docker-compose up -d mysql
```

然后本地运行前后端应用，连接到 Docker 中的 MySQL（localhost:3306）。

## 维护和更新

### 更新代码

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动
docker-compose up -d --build
```

### 清理未使用的资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune

# 清理所有未使用的资源
docker system prune -a --volumes
```

## 监控和日志

### 查看容器资源使用

```bash
docker stats
```

### 持久化日志

Docker Compose 默认使用 JSON file driver。可以配置日志轮转：

在 `docker-compose.yml` 中添加：
```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 外部监控

可以集成以下监控工具：
- **Prometheus + Grafana**: 指标监控和可视化
- **ELK Stack**: 日志聚合和分析
- **Swarmpit**: Docker Swarm 可视化管理

## 卸载

```bash
# 停止并删除所有容器、网络、数据卷
docker-compose down -v

# 删除构建的镜像
docker rmi carpool-backend carpool-frontend

# 删除项目文件
cd ..
rm -rf carpool
```

## 支持和反馈

如有问题，请查看：
1. 项目 GitHub Issues
2. Docker 日志: `docker-compose logs`
3. 项目文档: `CLAUDE.md`

## 附录

### 端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|---------|---------|------|
| frontend | 80 | 80 | 前端 Web 服务 |
| backend | 8080 | 8080 | 后端 API 服务 |
| mysql | 3306 | 3306 | MySQL 数据库 |
| collector | - | - | 数据采集服务（无端口暴露）|

### 数据卷

| 卷名 | 用途 |
|------|------|
| mysql_data | MySQL 数据持久化 |
| backend_logs | 后端日志持久化 |
| collector_logs | 数据采集日志持久化 |

### 网络

- **网络名**: `carpool-network`
- **驱动**: bridge
- **服务**: 所有服务在同一网络中，可以通过服务名互相访问

### 默认账户

**MySQL**:
- Root: root / root_password_114514
- 应用: carpool / carpool_password

**演示用户** (数据库初始化创建):
- Username: demo
- Password: demo123
- ⚠️ 生产环境请删除或修改此用户
