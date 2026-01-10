# 数据采集服务

本目录包含交通数据采集脚本，用于从百度地图 API 获取实时路况数据并存储到数据库。

## 文件说明

- `run.py` - 原始数据采集脚本（用于本地开发）
- `run_docker.py` - Docker 版本数据采集脚本（用于生产环境）
- `requirements.txt` - Python 依赖包列表
- `Dockerfile` - Docker 镜像构建文件
- `config.json` - 配置文件（需手动配置百度 API Key）
- `.dockerignore` - Docker 构建忽略文件
- `init.sql` - 数据库初始化脚本（在项目根目录的 script/ 文件夹）

## 配置说明

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置百度地图 API Key

编辑 `config.json` 文件：

```json
{
  "database": {
    "host": "localhost",
    "user": "root",
    "password": "your_password",
    "database": "carpool",
    "charset": "utf8mb4"
  },
  "baidu_api": {
    "ak": "YOUR_BAIDU_API_KEY"
  },
  "roads": [
    {"road_name": "四平路", "city": "上海市"},
    {"road_name": "中山北二路", "city": "上海市"}
  ],
  "schedule": {
    "collection_interval_seconds": 300,
    "request_interval_seconds": 2
  }
}
```

**配置项说明**:

- `database`: 数据库连接配置
  - `host`: 数据库主机地址（Docker 环境使用 `mysql`）
  - `user`: 数据库用户名
  - `password`: 数据库密码
  - `database`: 数据库名称
  - `charset`: 字符集

- `baidu_api`: 百度地图 API 配置
  - `ak`: 百度地图 API Key（必填）

- `roads`: 监控道路列表
  - `road_name`: 道路名称
  - `city`: 城市名称

- `schedule`: 采集调度配置
  - `collection_interval_seconds`: 每轮采集间隔（秒），默认 300 秒（5 分钟）
  - `request_interval_seconds`: 每个道路请求间隔（秒），默认 2 秒

## 获取百度地图 API Key

1. 访问 [百度地图开放平台](https://lbsyun.baidu.com/)
2. 注册/登录账号
3. 进入"控制台" → "应用管理" → "我的应用"
4. 点击"创建应用"
5. 填写应用信息：
   - 应用名称：自定义（如"交通监控"）
   - 应用类型：选择"服务端"
   - 启用服务：勾选"路况交通" API
6. 提交后，在应用列表中复制 AK (API Key)

**注意**:
- 免费版每日有配额限制（通常 10 万次/天）
- 生产环境建议购买商业版或控制采集频率
- API 调用频率限制：建议每秒不超过 2 次

## 本地运行

### 前置要求

- Python 3.7+
- MySQL 数据库已创建表结构

### 运行脚本

```bash
# 使用原始脚本（从代码中读取配置）
python run.py

# 或使用 Docker 版本（从配置文件读取）
python run_docker.py
```

### 脚本行为

脚本启动后会：
1. 连接到 MySQL 数据库
2. 遍历 `config.json` 中配置的道路列表
3. 对每条道路调用百度地图 API 获取路况数据
4. 将数据保存到 `road_traffic_overall` 和 `congestion_sections` 表
5. 等待配置的间隔时间后重复上述过程

## Docker 部署

### 使用 Docker Compose（推荐）

在项目根目录执行：

```bash
# 启动所有服务（包括数据采集服务）
docker-compose up -d

# 查看采集服务日志
docker-compose logs -f collector

# 查看日志文件
docker exec carpool-collector tail -f /app/logs/collector.log
```

### 单独构建和运行

```bash
# 构建镜像
cd script
docker build -t carpool-collector .

# 运行容器
docker run -d \
  --name carpool-collector \
  --network carpool-network \
  -v $(pwd)/config.json:/app/config.json:ro \
  -v collector_logs:/app/logs \
  carpool-collector
```

## 监控和日志

### 查看实时日志

```bash
# Docker Compose 环境
docker-compose logs -f collector

# 查看容器内日志文件
docker exec carpool-collector tail -f /app/logs/collector.log
```

### 日志内容

日志包含以下信息：
- 数据采集开始/完成时间
- 每条道路的数据获取状态
- API 请求成功/失败信息
- 数据库保存成功/失败信息
- 错误和异常信息

### 检查数据

```bash
# 连接到数据库查看最新采集的数据
docker exec -it carpool-mysql mysql -ucarpool -pcarpool_password carpool

# 在 MySQL 中执行
SELECT * FROM road_traffic_overall ORDER BY created_at DESC LIMIT 10;
SELECT COUNT(*) as total_records FROM road_traffic_overall;
SELECT city, road_name, COUNT(*) as count
FROM road_traffic_overall
GROUP BY city, road_name
ORDER BY count DESC;
```

## 配置调整

### 修改采集频率

编辑 `config.json`:

```json
{
  "schedule": {
    "collection_interval_seconds": 600,  // 改为 10 分钟
    "request_interval_seconds": 3        // 每个请求间隔 3 秒
  }
}
```

修改后重启容器：
```bash
docker-compose restart collector
```

### 添加监控道路

在 `config.json` 的 `roads` 数组中添加：

```json
{
  "roads": [
    {"road_name": "四平路", "city": "上海市"},
    {"road_name": "延安高架路", "city": "上海市"},
    {"road_name": "南京东路", "city": "上海市"},
    {"road_name": "世纪大道", "city": "上海市"}
  ]
}
```

修改后重启容器生效。

### 禁用数据采集服务

如需暂时禁用数据采集服务，在 `docker-compose.yml` 中注释掉 collector 服务：

```yaml
# # 数据采集服务
# collector:
#   build:
#     context: ./script
#     dockerfile: Dockerfile
#   ...
```

然后执行：
```bash
docker-compose up -d
```

## 故障排查

### 问题 1: API Key 无效

**错误信息**: `API返回错误: AK不存在`

**解决方案**:
1. 检查 `config.json` 中的 `ak` 是否正确
2. 确认百度地图控制台中应用已启用"路况交通"服务
3. 检查 API Key 是否已过期或被禁用

### 问题 2: 数据库连接失败

**错误信息**: `数据库连接失败`

**解决方案**:
1. 确认 MySQL 容器正在运行：`docker-compose ps mysql`
2. 检查数据库配置是否正确
3. 确认数据库已创建表结构
4. 检查网络连接：`docker network inspect carpool-network`

### 问题 3: 采集频率过高导致配额用尽

**错误信息**: `API返回错误: 日配额超限`

**解决方案**:
1. 增加 `collection_interval_seconds` 间隔时间
2. 减少监控道路数量
3. 升级百度地图 API 套餐
4. 暂时禁用采集服务

### 问题 4: 时区不正确

**解决方案**:
- 容器已配置为 `Asia/Shanghai` 时区
- 检查系统时间：`docker exec carpool-collector date`
- 如需修改，在 `docker-compose.yml` 中调整 `TZ` 环境变量

## 性能优化

### 1. 调整采集间隔

根据实际需求和 API 配额调整采集频率：
- 测试环境：10-15 分钟
- 生产环境：5-10 分钟
- 高频监控：2-5 分钟（需确保 API 配额充足）

### 2. 批量处理

当前版本每次请求间隔 2 秒，可调整 `request_interval_seconds` 控制请求速率。

### 3. 数据清理

定期清理历史数据避免数据库膨胀：

```sql
-- 删除 30 天前的数据
DELETE FROM congestion_sections
WHERE overall_id IN (
    SELECT id FROM road_traffic_overall
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
);

DELETE FROM road_traffic_overall
WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 开发和测试

### 本地测试脚本

```bash
# 设置虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt

# 运行脚本
python run_docker.py
```

### 单次测试

修改 `run_docker.py`，将 `while True` 循环改为单次执行，或设置较短的间隔时间进行测试。

## 许可和支持

- 百度地图 API 服务条款: https://lbsyun.baidu.com/apiconsole/key
- 项目 Issues: 在项目 GitHub 仓库提交问题
- 数据采集脚本基于百度地图 API，需遵守其使用规范
