# 行程管理 API 文档

## 概述

本文档描述了行程管理相关的API接口，包括查询用户行程、取消行程等功能。

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: Bearer Token (JWT)
- **Content-Type**: `application/json`

## API 接口

### 1. 获取当前用户的所有行程

获取当前登录用户参与的所有行程记录。

**请求**
```
GET /api/trip/user
```

**请求头**
```
Authorization: Bearer <token>
```

**响应示例**
```json
[
  {
    "id": 1,
    "startLocation": "上海市浦东国际机场T2航站楼",
    "startLatitude": 31.1443,
    "startLongitude": 121.8083,
    "endLocation": "上海市虹桥火车站",
    "endLatitude": 31.1979,
    "endLongitude": 121.3206,
    "departureAt": "2025-12-28 14:30:00",
    "statusDesc": "已创建",
    "passengerCount": 3,
    "matchAt": "2025-12-27 10:15:00",
    "createdAt": "2025-12-27 09:30:00",
    "requestIds": [1, 2]
  },
  {
    "id": 2,
    "startLocation": "上海站",
    "startLatitude": 31.2494,
    "startLongitude": 121.4568,
    "endLocation": "上海迪士尼度假区",
    "endLatitude": 31.1434,
    "endLongitude": 121.6570,
    "departureAt": "2025-12-29 09:00:00",
    "statusDesc": "已到达",
    "passengerCount": 4,
    "matchAt": "2025-12-26 15:20:00",
    "createdAt": "2025-12-26 14:00:00",
    "requestIds": [3]
  }
]
```

**响应状态码**
- `200 OK`: 成功获取行程列表
- `401 Unauthorized`: 未提供认证令牌或令牌无效
- `500 Internal Server Error`: 服务器内部错误

### 2. 获取行程详情

根据行程ID获取单个行程的详细信息。

**请求**
```
GET /api/trip/{tripId}
```

**路径参数**
- `tripId`: 行程ID (Long)

**响应示例**
```json
{
  "id": 1,
  "startLocation": "上海市浦东国际机场T2航站楼",
  "startLatitude": 31.1443,
  "startLongitude": 121.8083,
  "endLocation": "上海市虹桥火车站",
  "endLatitude": 31.1979,
  "endLongitude": 121.3206,
  "departureAt": "2025-12-28 14:30:00",
  "statusDesc": "已创建",
  "passengerCount": 3,
  "matchAt": "2025-12-27 10:15:00",
  "createdAt": "2025-12-27 09:30:00",
  "requestIds": [1, 2]
}
```

**响应状态码**
- `200 OK`: 成功获取行程详情
- `404 Not Found`: 行程不存在
- `500 Internal Server Error`: 服务器内部错误

### 3. 根据拼车需求ID获取行程

根据拼车需求ID获取关联的行程信息。

**请求**
```
GET /api/trip/request/{requestId}
```

**路径参数**
- `requestId`: 拼车需求ID (Long)

**响应示例**
```json
{
  "id": 1,
  "startLocation": "上海市浦东国际机场T2航站楼",
  "startLatitude": 31.1443,
  "startLongitude": 121.8083,
  "endLocation": "上海市虹桥火车站",
  "endLatitude": 31.1979,
  "endLongitude": 121.3206,
  "departureAt": "2025-12-28 14:30:00",
  "statusDesc": "已创建",
  "passengerCount": 3,
  "matchAt": "2025-12-27 10:15:00",
  "createdAt": "2025-12-27 09:30:00",
  "requestIds": [1]
}
```

**响应状态码**
- `200 OK`: 成功获取行程
- `404 Not Found`: 该拼车需求还没有行程
- `500 Internal Server Error`: 服务器内部错误

### 4. 更新行程状态

更新指定行程的状态。

**请求**
```
PUT /api/trip/{tripId}/status
```

**路径参数**
- `tripId`: 行程ID (Long)

**请求头**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**
```json
{
  "statusDesc": "已出发"
}
```

**状态值说明**
- `已创建`: 行程已创建但未出发
- `已出发`: 行程已出发
- `已到达`: 行程已到达目的地
- `已取消`: 行程已取消

**响应示例**
```json
{
  "id": 1,
  "startLocation": "上海市浦东国际机场T2航站楼",
  "startLatitude": 31.1443,
  "startLongitude": 121.8083,
  "endLocation": "上海市虹桥火车站",
  "endLatitude": 31.1979,
  "endLongitude": 121.3206,
  "departureAt": "2025-12-28 14:30:00",
  "statusDesc": "已出发",
  "passengerCount": 3,
  "matchAt": "2025-12-27 10:15:00",
  "createdAt": "2025-12-27 09:30:00",
  "requestIds": [1, 2]
}
```

**响应状态码**
- `200 OK`: 成功更新行程状态
- `401 Unauthorized`: 未提供认证令牌或令牌无效
- `403 Forbidden`: 用户没有权限操作此行程（未参与该行程）
- `400 Bad Request`: 无效的行程状态
- `500 Internal Server Error`: 服务器内部错误

### 5. 取消行程

取消指定的行程（将状态设置为"已取消"）。

**请求**
```
PUT /api/trip/{tripId}/cancel
```

**路径参数**
- `tripId`: 行程ID (Long)

**请求头**
```
Authorization: Bearer <token>
```

**响应示例**
```json
{
  "id": 1,
  "startLocation": "上海市浦东国际机场T2航站楼",
  "startLatitude": 31.1443,
  "startLongitude": 121.8083,
  "endLocation": "上海市虹桥火车站",
  "endLatitude": 31.1979,
  "endLongitude": 121.3206,
  "departureAt": "2025-12-28 14:30:00",
  "statusDesc": "已取消",
  "passengerCount": 3,
  "matchAt": "2025-12-27 10:15:00",
  "createdAt": "2025-12-27 09:30:00",
  "requestIds": [1, 2]
}
```

**响应状态码**
- `200 OK`: 成功取消行程
- `401 Unauthorized`: 未提供认证令牌或令牌无效
- `403 Forbidden`: 用户没有权限操作此行程（未参与该行程）
- `400 Bad Request`: 取消失败
- `500 Internal Server Error`: 服务器内部错误

## 数据模型

### TripResponse

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 行程ID |
| startLocation | String | 起点位置 |
| startLatitude | Double | 起点纬度 |
| startLongitude | Double | 起点经度 |
| endLocation | String | 终点位置 |
| endLatitude | Double | 终点纬度 |
| endLongitude | Double | 终点经度 |
| departureAt | String | 出发时间 (yyyy-MM-dd HH:mm:ss) |
| statusDesc | String | 行程状态描述 |
| passengerCount | Integer | 乘客总数 |
| matchAt | String | 匹配时间 (yyyy-MM-dd HH:mm:ss) |
| createdAt | String | 创建时间 (yyyy-MM-dd HH:mm:ss) |
| requestIds | Array\<Long\> | 关联的拼车需求ID列表 |

## 前端使用示例

### 获取用户行程

```javascript
import axios from 'axios'

// 获取当前用户的所有行程
async function getUserTrips() {
  try {
    const response = await axios.get('http://localhost:8080/api/trip/user', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    return response.data
  } catch (error) {
    console.error('获取行程失败:', error)
    throw error
  }
}

// 使用示例
const trips = await getUserTrips()
console.log('用户行程:', trips)
```

### 取消行程

```javascript
// 取消行程
async function cancelTrip(tripId) {
  try {
    const response = await axios.put(
      `http://localhost:8080/api/trip/${tripId}/cancel`,
      {},
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    )
    return response.data
  } catch (error) {
    console.error('取消行程失败:', error)
    throw error
  }
}

// 使用示例
await cancelTrip(1)
console.log('行程已取消')
```

### 在 Vue 组件中使用

```vue
<template>
  <div>
    <TripCardGrid
      :trips="trips"
      :loading="loading"
      @view="handleViewTrip"
      @cancel="handleCancelTrip"
    />
  </div>
</template>

<script>
import axios from 'axios'
import TripCardGrid from '@/components/TripCardGrid.vue'

export default {
  components: { TripCardGrid },
  data() {
    return {
      trips: [],
      loading: false
    }
  },
  mounted() {
    this.fetchTrips()
  },
  methods: {
    async fetchTrips() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/api/trip/user', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.trips = response.data
      } catch (error) {
        console.error('获取行程失败:', error)
      } finally {
        this.loading = false
      }
    },

    handleViewTrip(trip) {
      this.$router.push(`/trip/${trip.id}`)
    },

    async handleCancelTrip(trip) {
      try {
        await axios.put(`http://localhost:8080/api/trip/${trip.id}/cancel`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        alert('行程已取消')
        this.fetchTrips() // 刷新列表
      } catch (error) {
        alert('取消行程失败: ' + (error.response?.data?.message || error.message))
      }
    }
  }
}
</script>
```

## 注意事项

1. **认证要求**: 所有需要用户身份的接口都必须在请求头中提供有效的JWT token
2. **权限验证**: 更新和取消行程接口需要验证用户是否参与了该行程
   - 只有参与该行程的用户（在 match_record 表中有记录）才能更新或取消行程
   - 如果用户未参与该行程，将返回 403 Forbidden 错误
3. **时间格式**: 所有时间字段使用 `yyyy-MM-dd HH:mm:ss` 格式
4. **状态管理**: 行程状态只能更新为预定义的状态值之一
5. **关联查询**: 用户行程通过 `match_record` 表关联查询，返回的是用户参与的所有行程
6. **错误处理**: 前端应正确处理各种错误状态码，并给用户友好的提示

## 权限验证说明

### 验证逻辑

系统通过以下步骤验证用户是否有权限操作行程：

1. **Token 验证**: 验证 JWT token 是否有效
2. **用户ID提取**: 从 token 中提取用户ID
3. **行程参与验证**: 检查用户是否在 `match_record` 表中与该行程关联
   ```sql
   SELECT * FROM match_record WHERE trip_id = ? AND user_id = ?
   ```
4. **权限判断**:
   - 如果找到匹配记录 → 用户有权限
   - 如果未找到匹配记录 → 返回 403 Forbidden

### 权限规则

- ✅ **有权限**: 用户参与了该行程（作为车主或乘客）
- ❌ **无权限**: 用户未参与该行程

### 错误处理

当用户无权限时，API 返回：
```json
{
  "message": "您没有权限操作此行程"
}
```
HTTP 状态码: `403 Forbidden`

### 前端处理建议

```javascript
try {
  await axios.put(`/api/trip/${tripId}/cancel`, {}, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
} catch (error) {
  if (error.response?.status === 403) {
    alert('您没有权限操作此行程')
  } else {
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}
```

## 数据库表结构

### trip_record

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | BIGINT | 主键 |
| start_location | VARCHAR(255) | 起点位置 |
| start_latitude | DOUBLE | 起点纬度 |
| start_longitude | DOUBLE | 起点经度 |
| end_location | VARCHAR(255) | 终点位置 |
| end_latitude | DOUBLE | 终点纬度 |
| end_longitude | DOUBLE | 终点经度 |
| departure_at | DATETIME | 出发时间 |
| status_desc | VARCHAR(50) | 状态描述 |
| passenger_count | INT | 乘客总数 |
| match_at | DATETIME | 匹配时间 |
| created_at | DATETIME | 创建时间 |

### match_record

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | BIGINT | 主键 |
| request_id | BIGINT | 拼车需求ID (外键) |
| user_id | BIGINT | 用户ID (外键) |
| trip_id | BIGINT | 行程ID (外键) |
| created_at | DATETIME | 创建时间 |
