# 历史路况数据 API 设计

## 概述

本文档描述了为历史路况显示卡片设计的后端API接口。这些接口用于获取指定道路在一段时间内的历史路况数据，以及支持的城市和道路信息。

## API 接口

### 1. 获取历史路况数据

**接口地址**: `GET /api/traffic/historical`

**功能描述**: 获取指定道路在指定时间范围内的历史路况数据

**请求参数**:
```
roadName: string (必需) - 道路名称
city: string (必需) - 城市名称
startTime: string (必需) - 开始时间，ISO 8601格式
endTime: string (必需) - 结束时间，ISO 8601格式
page: int (可选，默认0) - 页码
size: int (可选，默认100) - 每页大小
```

**请求示例**:
```http
GET /api/traffic/historical?roadName=长安街&city=北京&startTime=2024-12-20T08:00:00Z&endTime=2024-12-21T08:00:00Z&page=0&size=100
```

**响应数据结构**:
```json
{
  "content": [
    {
      "id": 12345,
      "roadName": "长安街",
      "city": "北京",
      "evaluationStatus": 2,
      "evaluationStatusDesc": "缓行",
      "description": "道路车流量较大，通行缓慢",
      "requestTime": "2024-12-20T08:30:00Z",
      "speed": 25.5,
      "congestionDistance": 1.2,
      "statusText": "缓行"
    }
    // ... 更多数据点
  ],
  "totalElements": 150,
  "totalPages": 2,
  "size": 100,
  "number": 0,
  "first": true,
  "last": false
}
```

**数据字段说明**:
- `id`: 记录ID
- `roadName`: 道路名称
- `city`: 城市名称
- `evaluationStatus`: 拥堵状态 (0:未知, 1:畅通, 2:缓行, 3:拥堵, 4:严重拥堵)
- `evaluationStatusDesc`: 拥堵状态描述
- `description`: 路况描述
- `requestTime`: 数据采集时间
- `speed`: 平均速度 (km/h)
- `congestionDistance`: 拥堵距离 (km)
- `statusText`: 状态文本

---

### 2. 获取城市道路列表

**接口地址**: `GET /api/traffic/cities/{city}/roads`

**功能描述**: 获取指定城市的所有道路列表

**路径参数**:
```
city: string - 城市名称
```

**请求示例**:
```http
GET /api/traffic/cities/北京/roads
```

**响应数据结构**:
```json
[
  "长安街",
  "二环路",
  "三环路",
  "四环路",
  "五环路",
  "建国门内大街",
  "复兴门内大街"
]
```

---

### 3. 获取支持的城市列表

**接口地址**: `GET /api/traffic/cities`

**功能描述**: 获取系统支持的所有城市列表

**请求示例**:
```http
GET /api/traffic/cities
```

**响应数据结构**:
```json
[
  "北京",
  "上海",
  "广州",
  "深圳",
  "杭州",
  "成都",
  "南京",
  "武汉"
]
```

## 后端实现建议

### Controller 层

在 `TrafficController.java` 中添加以下方法：

```java
/**
 * 获取历史路况数据
 */
@GetMapping("/historical")
public ResponseEntity<Page<TrafficResponse>> getHistoricalTraffic(
        @RequestParam String roadName,
        @RequestParam String city,
        @RequestParam String startTime,
        @RequestParam String endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size) {
    // 实现逻辑
}

/**
 * 获取城市道路列表
 */
@GetMapping("/cities/{city}/roads")
public ResponseEntity<List<String>> getRoadsByCity(@PathVariable String city) {
    // 实现逻辑
}

/**
 * 获取支持的城市列表
 */
@GetMapping("/cities")
public ResponseEntity<List<String>> getSupportedCities() {
    // 实现逻辑
}
```

### Service 层

在 `TrafficService.java` 中添加相应的方法：

```java
public Page<TrafficResponse> getHistoricalTraffic(
        String roadName, String city,
        LocalDateTime startTime, LocalDateTime endTime,
        Pageable pageable);

public List<String> getRoadsByCity(String city);

public List<String> getSupportedCities();
```

### Repository 层

可能需要添加新的查询方法：

```java
// 在 RoadTrafficOverallRepository 中添加
@Query("SELECT r FROM RoadTrafficOverall r WHERE " +
       "r.roadName = :roadName AND r.city = :city AND " +
       "r.requestTime BETWEEN :startTime AND :endTime " +
       "ORDER BY r.requestTime DESC")
Page<RoadTrafficOverall> findHistoricalTraffic(
    @Param("roadName") String roadName,
    @Param("city") String city,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    Pageable pageable);

// 获取城市列表
@Query("SELECT DISTINCT r.city FROM RoadTrafficOverall r")
List<String> findDistinctCities();

// 获取指定城市的道路列表
@Query("SELECT DISTINCT r.roadName FROM RoadTrafficOverall r WHERE r.city = :city")
List<String> findDistinctRoadsByCity(@Param("city") String city);
```

## 数据库索引优化建议

为了提高历史数据查询性能，建议添加以下索引：

```sql
-- 复合索引用于历史数据查询
CREATE INDEX idx_road_city_time ON road_traffic_overall (road_name, city, request_time);

-- 城市索引
CREATE INDEX idx_city ON road_traffic_overall (city);

-- 道路索引
CREATE INDEX idx_road_name ON road_traffic_overall (road_name);

-- 时间索引用于时间范围查询
CREATE INDEX idx_request_time ON road_traffic_overall (request_time);
```

## 缓存策略建议

1. **城市和道路列表缓存**: 这些数据变化不频繁，可以缓存较长时间（如1小时）

2. **历史数据缓存**: 根据时间范围设置不同的缓存策略
   - 1小时内数据：缓存5分钟
   - 1天内数据：缓存30分钟
   - 超过1天的数据：缓存2小时

3. **统计结果缓存**: 对计算密集的统计数据进行缓存

## 错误处理

1. **参数验证**: 检查必需参数是否提供
2. **时间格式验证**: 验证时间字符串格式
3. **时间范围验证**: 确保开始时间早于结束时间
4. **数据权限**: 验证用户是否有权限访问指定数据
5. **性能保护**: 限制查询的最大时间范围（如不超过30天）

## 性能优化

1. **分页查询**: 大量历史数据必须使用分页
2. **索引优化**: 确保查询字段有合适的索引
3. **数据压缩**: 响应数据可以使用Gzip压缩
4. **异步处理**: 对于大量数据查询，考虑异步处理
5. **限流保护**: 防止API被滥用

## 监控指标

1. **API响应时间**: 监控不同时间范围查询的响应时间
2. **缓存命中率**: 监控缓存策略的效果
3. **查询数据量**: 监控查询返回的数据量
4. **错误率**: 监控API调用失败率

## 安全考虑

1. **输入验证**: 防止SQL注入和XSS攻击
2. **访问控制**: 实现适当的认证和授权机制
3. **数据脱敏**: 对敏感信息进行脱敏处理
4. **请求限制**: 实现合理的API调用频率限制