# 历史路况API测试文档

## 概述

本文档提供了历史路况相关API接口的测试方法和示例。后端接口已经完成实现，需要启动Spring Boot应用进行测试。

## 启动后端应用

```bash
cd carpool-b
./gradlew bootRun
```

应用将在 `http://localhost:8080` 启动。

## API 接口测试

### 1. 获取支持的城市列表

**接口**: `GET /api/traffic/cities`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/traffic/cities"
```

**预期响应**:
```json
[
  "上海",
  "北京",
  "广州",
  "深圳",
  "杭州"
]
```

### 2. 获取指定城市的道路列表

**接口**: `GET /api/traffic/cities/{city}/roads`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/traffic/cities/上海/roads"
```

**预期响应**:
```json
[
  "南京路",
  "淮海路",
  "延安高架",
  "内环高架",
  "中环路",
  "南北高架",
  "逸仙高架"
]
```

### 3. 获取历史路况数据

**接口**: `GET /api/traffic/historical`

**请求参数**:
- `roadName`: 道路名称 (必需)
- `city`: 城市名称 (必需)
- `startTime`: 开始时间，ISO格式 (必需)
- `endTime`: 结束时间，ISO格式 (必需)
- `page`: 页码，默认0 (可选)
- `size`: 每页大小，默认100 (可选)

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=2024-12-20T00:00:00&endTime=2024-12-21T00:00:00&page=0&size=50"
```

**预期响应**:
```json
{
  "content": [
    {
      "id": 12345,
      "roadName": "南京路",
      "city": "上海",
      "evaluationStatus": 2,
      "evaluationStatusDesc": "缓行",
      "description": "道路车流量较大，通行缓慢",
      "requestTime": "2024-12-20 14:30:00",
      "speed": 25.5,
      "congestionDistance": 1,
      "statusText": "缓行"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

### 4. 测试不同的时间范围

**最近1小时**:
```bash
# 获取当前时间的ISO格式
currentTime=$(date -u +"%Y-%m-%dT%H:%M:%S")
oneHourAgo=$(date -u -d '1 hour ago' +"%Y-%m-%dT%H:%M:%S")

curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${oneHourAgo}&endTime=${currentTime}"
```

**最近6小时**:
```bash
sixHoursAgo=$(date -u -d '6 hours ago' +"%Y-%m-%dT%H:%M:%S")
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${sixHoursAgo}&endTime=${currentTime}"
```

**最近1天**:
```bash
oneDayAgo=$(date -u -d '1 day ago' +"%Y-%m-%dT%H:%M:%S")
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${oneDayAgo}&endTime=${currentTime}"
```

## 错误处理测试

### 1. 参数验证错误

**缺少必需参数**:
```bash
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路"
```

**预期响应**: 400错误，包含错误信息

**时间格式错误**:
```bash
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=2024-12-20&endTime=2024-12-21"
```

**预期响应**: 400错误，时间格式不正确

### 2. 业务逻辑错误

**道路不存在**:
```bash
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=不存在的道路&city=上海&startTime=2024-12-20T00:00:00&endTime=2024-12-21T00:00:00"
```

**预期响应**: 400错误，道路不存在

**城市不存在**:
```bash
curl -X GET "http://localhost:8080/api/traffic/cities/不存在的城市/roads"
```

**预期响应**: 400错误，城市暂无数据

### 3. 性能限制测试

**时间范围过大**:
```bash
# 查询超过30天的数据
thirtyOneDaysAgo=$(date -u -d '31 days ago' +"%Y-%m-%dT%H:%M:%S")
curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${thirtyOneDaysAgo}&endTime=${currentTime}"
```

**预期响应**: 400错误，查询时间范围不能超过30天

## 性能测试

### 1. 响应时间测试

使用 `time` 命令测量响应时间：
```bash
time curl -X GET "http://localhost:8080/api/traffic/cities"
time curl -X GET "http://localhost:8080/api/traffic/cities/上海/roads"
time curl -X GET "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${oneDayAgo}&endTime=${currentTime}"
```

### 2. 并发测试

使用 Apache Bench 进行简单的并发测试：
```bash
# 安装ab工具 (如果没有)
# Ubuntu/Debian: sudo apt-get install apache2-utils
# macOS: brew install apache2-utils

# 对城市列表接口进行100次并发测试
ab -n 100 -c 10 "http://localhost:8080/api/traffic/cities"

# 对历史数据接口进行并发测试
ab -n 50 -c 5 "http://localhost:8080/api/traffic/historical?roadName=南京路&city=上海&startTime=${oneDayAgo}&endTime=${currentTime}"
```

## 数据库索引验证

### 1. 查看执行计划

连接到MySQL数据库并验证索引使用：
```sql
-- 查看历史数据查询的执行计划
EXPLAIN SELECT * FROM road_traffic_overall
WHERE road_name = '南京路' AND city = '上海'
AND request_time BETWEEN '2024-12-20 00:00:00' AND '2024-12-21 00:00:00'
ORDER BY request_time DESC;

-- 查看城市列表查询的执行计划
EXPLAIN SELECT DISTINCT city FROM road_traffic_overall;

-- 查看道路列表查询的执行计划
EXPLAIN SELECT DISTINCT road_name FROM road_traffic_overall WHERE city = '上海';
```

### 2. 索引使用统计

```sql
-- 查看索引使用情况
SELECT
  TABLE_NAME,
  INDEX_NAME,
  CARDINALITY,
  SUB_PART,
  NULLABLE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('road_traffic_overall', 'congestion_sections')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
```

## 前端集成测试

### 1. 启动前端应用

```bash
cd carpool-f
npm install
npm run dev
```

### 2. 访问历史分析页面

打开浏览器访问：`http://localhost:5173/historical`

### 3. 功能测试

1. **城市选择测试**:
   - 点击城市下拉框
   - 验证城市列表是否正确加载
   - 选择不同城市

2. **道路选择测试**:
   - 选择城市后，验证道路列表是否正确加载
   - 选择不同道路

3. **时间范围测试**:
   - 点击不同的时间范围按钮
   - 验证图表数据是否相应更新

4. **图表显示测试**:
   - 验证图表是否正确显示拥堵指数曲线
   - 验证速度曲线是否正确显示
   - 验证统计信息是否正确计算

5. **错误处理测试**:
   - 断开网络连接，查看错误提示
   - 模拟API错误，查看错误处理

## 常见问题排查

### 1. 接口返回404错误

**原因**: 路径错误或应用未启动
**解决**:
- 确认Spring Boot应用正在运行
- 检查API路径是否正确
- 确认CORS配置是否正确

### 2. 数据库连接错误

**原因**: 数据库未启动或连接配置错误
**解决**:
- 检查MySQL服务是否运行
- 验证 `application.properties` 中的数据库配置
- 确认数据库表和数据是否存在

### 3. 前端无法访问后端API

**原因**: CORS配置或网络问题
**解决**:
- 检查后端的CORS配置
- 确认端口和域名配置
- 检查防火墙设置

### 4. 查询性能问题

**原因**: 缺少索引或数据量过大
**解决**:
- 执行数据库索引创建脚本
- 优化查询参数
- 考虑添加缓存机制

## 测试报告模板

```markdown
# 历史路况API测试报告

## 测试环境
- 后端版本:
- 数据库版本:
- 测试时间:
- 测试人员:

## 功能测试结果

### 城市列表接口
- [ ] 正常返回
- [ ] 错误处理
- [ ] 性能测试

### 道路列表接口
- [ ] 正常返回
- [ ] 参数验证
- [ ] 错误处理

### 历史数据接口
- [ ] 正常查询
- [ ] 时间范围限制
- [ ] 分页功能
- [ ] 数据完整性
- [ ] 性能测试

## 前端集成测试
- [ ] 组件渲染
- [ ] 数据加载
- [ ] 交互功能
- [ ] 错误处理

## 性能指标
- 城市列表响应时间:
- 道路列表响应时间:
- 历史数据响应时间:
- 并发测试结果:

## 问题记录
1.
2.
3.

## 结论
```