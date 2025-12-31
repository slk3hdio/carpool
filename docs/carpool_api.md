# 后端拼车相关API列表

## 1. 拼车请求管理API

| 方法 | 接口路径 | 功能描述 | 权限要求 | 请求参数 | 响应数据 |
|------|---------|---------|---------|---------|---------|
| POST | `/api/carpool/request` | 发布拼车请求 | 已登录用户 | `CarpoolRequestDto`对象（包含出发地、目的地、时间等信息） | 创建的拼车请求对象 |
| GET | `/api/carpool/requests` | 搜索拼车请求 | 已登录用户 | - `statusDesc`: 请求状态<br>- `startLat`: 起点纬度<br>- `startLng`: 起点经度<br>- `radius`: 搜索半径(公里)<br>- `earliestTime`: 最早出发时间<br>- `latestTime`: 最晚出发时间 | 拼车请求响应列表（包含用户信息） |

## 2. 拼车邀请管理API

| 方法 | 接口路径 | 功能描述 | 权限要求 | 请求参数 | 响应数据 |
|------|---------|---------|---------|---------|---------|
| POST | `/api/carpool/invitation` | 创建拼车邀请 | 已登录用户 | `InvitationRequest`对象（包含发起者ID、拼车需求ID、人数、留言等） | 创建的邀请响应对象 |
| GET | `/api/carpool/invitation/request/{requestId}` | 获取某个拼车需求的所有邀请 | 已登录用户 | `requestId`: 拼车需求ID（路径参数） | 邀请响应列表 |
| GET | `/api/carpool/invitation/inviter/{inviterId}` | 获取用户发起的所有邀请 | 已登录用户 | `inviterId`: 发起者ID（路径参数） | 邀请响应列表 |
| GET | `/api/carpool/invitation/received/{userId}` | 获取用户收到的所有邀请 | 已登录用户 | `userId`: 用户ID（路径参数） | 邀请响应列表 |
| PUT | `/api/carpool/invitation/{id}/accept` | 接受邀请 | 已登录用户 | `id`: 邀请ID（路径参数） | 更新后的邀请响应对象 |
| PUT | `/api/carpool/invitation/{id}/reject` | 拒绝邀请 | 已登录用户 | `id`: 邀请ID（路径参数） | 更新后的邀请响应对象 |
| PUT | `/api/carpool/invitation/{id}/cancel` | 取消邀请 | 已登录用户 | `id`: 邀请ID（路径参数） | 更新后的邀请响应对象 |

## 3. 行程管理API

| 方法 | 接口路径 | 功能描述 | 权限要求 | 请求参数 | 响应数据 |
|------|---------|---------|---------|---------|---------|
| GET | `/api/trip/{tripId}` | 获取行程详情 | 已登录用户 | `tripId`: 行程ID（路径参数） | 行程响应对象 |
| GET | `/api/trip/request/{requestId}` | 根据拼车需求ID获取行程 | 已登录用户 | `requestId`: 拼车需求ID（路径参数） | 行程响应对象 |
| PUT | `/api/trip/{tripId}/status` | 更新行程状态 | 已登录用户 | - `tripId`: 行程ID（路径参数）<br>- `statusDesc`: 新状态描述 | 更新后的行程响应对象 |

## 4. 数据结构定义

### 4.1 CarpoolRequestDto
```json
{
  "userId": 1,
  "hasCar": true,
  "passengerCount": 1,
  "maxPassengerCount": 3,
  "startLocation": "北京市朝阳区",
  "startLatitude": 39.9042,
  "startLongitude": 116.4074,
  "endLocation": "北京市海淀区",
  "endLatitude": 39.9588,
  "endLongitude": 116.3084,
  "earliestDepartureTime": "2025-12-28 08:00:00",
  "latestDepartureTime": "2025-12-28 09:00:00",
  "phoneNumber": "13800138000",
  "statusDesc": "待匹配"
}
```

### 4.2 InvitationRequest
```json
{
  "inviterId": 2,
  "carpoolRequestId": 1,
  "passengerCount": 2,
  "message": "我可以带2个人，时间合适"
}
```

### 4.3 TripStatusUpdateRequest
```json
{
  "statusDesc": "已完成"
}
```

## 5. 错误响应格式

所有API在发生错误时返回统一的错误响应格式：

```json
{
  "message": "错误描述信息"
}
```

## 6. 状态码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 请求成功 |
| 201 | 资源创建成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 7. 权限控制

- 所有API都需要用户已登录
- 部分操作需要特定的权限（如修改行程状态需要是行程的参与者）
- 用户只能查看和操作自己相关的资源