# 行程权限验证测试用例

## 测试概述

本文档描述行程操作权限验证的测试用例，确保只有参与行程的用户才能更新或取消行程。

## 测试场景

### 场景1: 用户参与行程，有权限操作

**前置条件**:
- 用户A (ID=1) 参与了行程1
- 用户A 已登录，持有有效的 JWT token

**测试步骤**:
1. 用户A 调用 `PUT /api/trip/1/cancel`
2. 携带用户A的 JWT token

**预期结果**:
```json
{
  "id": 1,
  "statusDesc": "已取消",
  ...
}
```
HTTP Status: `200 OK`

### 场景2: 用户未参与行程，无权限操作

**前置条件**:
- 用户A (ID=1) 参与了行程1
- 用户B (ID=2) 未参与行程1
- 用户B 已登录，持有有效的 JWT token

**测试步骤**:
1. 用户B 调用 `PUT /api/trip/1/cancel`
2. 携带用户B的 JWT token

**预期结果**:
```json
{
  "message": "您没有权限操作此行程"
}
```
HTTP Status: `403 Forbidden`

### 场景3: 未提供 token

**测试步骤**:
1. 调用 `PUT /api/trip/1/cancel`
2. 不携带 Authorization 头

**预期结果**:
```json
{
  "message": "未提供认证令牌"
}
```
HTTP Status: `401 Unauthorized`

### 场景4: Token 无效

**测试步骤**:
1. 调用 `PUT /api/trip/1/cancel`
2. 携带无效的 JWT token

**预期结果**:
```json
{
  "message": "无效的认证令牌"
}
```
HTTP Status: `401 Unauthorized`

### 场景5: 行程不存在

**前置条件**:
- 用户A 已登录，持有有效的 JWT token

**测试步骤**:
1. 用户A 调用 `PUT /api/trip/99999/cancel`
2. 携带有效的 JWT token

**预期结果**:
```json
{
  "message": "行程不存在"
}
```
HTTP Status: `403 Forbidden` (或 400 Bad Request)

## SQL 测试数据准备

```sql
-- 创建测试用户
INSERT INTO users (username, password, phone_number, email, real_name, status, created_at, updated_at)
VALUES
  ('user1', '$2a$10$...', '13800138001', 'user1@test.com', '张三', 1, NOW(), NOW()),
  ('user2', '$2a$10$...', '13800138002', 'user2@test.com', '李四', 1, NOW(), NOW());

-- 创建拼车需求
INSERT INTO carpool_request (
  user_id, has_car, passenger_count, max_passenger_count,
  start_location, start_latitude, start_longitude,
  end_location, end_latitude, end_longitude,
  earliest_departure_time, latest_departure_time,
  phone_number, status_desc, created_at
) VALUES (
  1, true, 1, 4,
  '上海站', 31.2494, 121.4568,
  '上海迪士尼', 31.1434, 121.6570,
  '2025-12-29 09:00:00', '2025-12-29 10:00:00',
  '13800138001', '等待匹配', NOW()
);

-- 创建行程
INSERT INTO trip_record (
  start_location, start_latitude, start_longitude,
  end_location, end_latitude, end_longitude,
  departure_at, status_desc, passenger_count,
  match_at, created_at
) VALUES (
  '上海站', 31.2494, 121.4568,
  '上海迪士尼', 31.1434, 121.6570,
  '2025-12-29 09:00:00', '已创建', 2,
  NOW(), NOW()
);

-- 创建匹配记录（用户1参与行程）
INSERT INTO match_record (request_id, user_id, trip_id, created_at)
VALUES (1, 1, LAST_INSERT_ID(), NOW());

-- 注意：用户2 没有匹配记录，因此不能操作该行程
```

## cURL 测试命令

### 测试1: 有权限的用户取消行程

```bash
# 1. 登录获取 token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}' \
  | jq -r '.token')

# 2. 取消行程
curl -X PUT http://localhost:8080/api/trip/1/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期响应**:
```json
{
  "id": 1,
  "statusDesc": "已取消",
  ...
}
```

### 测试2: 无权限的用户尝试取消行程

```bash
# 1. 登录用户2获取 token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user2","password":"password123"}' \
  | jq -r '.token')

# 2. 尝试取消行程1
curl -X PUT http://localhost:8080/api/trip/1/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**预期响应**:
```json
{
  "message": "您没有权限操作此行程"
}
```

### 测试3: 无效 token

```bash
curl -X PUT http://localhost:8080/api/trip/1/cancel \
  -H "Authorization: Bearer invalid_token_12345" \
  -H "Content-Type: application/json"
```

**预期响应**:
```json
{
  "message": "无效的认证令牌"
}
```

### 测试4: 无 token

```bash
curl -X PUT http://localhost:8080/api/trip/1/cancel \
  -H "Content-Type: application/json"
```

**预期响应**:
```json
{
  "message": "未提供认证令牌"
}
```

## Postman 测试集合

```json
{
  "info": {
    "name": "行程权限验证测试",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "登录 - 用户1",
      "request": {
        "method": "POST",
        "header": [{"key":"Content-Type","value":"application/json"}],
        "url": "{{base_url}}/auth/login",
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"user1\",\"password\":\"password123\"}"
        }
      }
    },
    {
      "name": "取消行程 - 有权限",
      "request": {
        "method": "PUT",
        "header": [
          {"key":"Authorization","value":"Bearer {{user1_token}}"},
          {"key":"Content-Type","value":"application/json"}
        ],
        "url": "{{base_url}}/trip/1/cancel"
      }
    },
    {
      "name": "取消行程 - 无权限",
      "request": {
        "method": "PUT",
        "header": [
          {"key":"Authorization","value":"Bearer {{user2_token}}"},
          {"key":"Content-Type","value":"application/json"}
        ],
        "url": "{{base_url}}/trip/1/cancel"
      }
    },
    {
      "name": "取消行程 - 无Token",
      "request": {
        "method": "PUT",
        "header": [{"key":"Content-Type","value":"application/json"}],
        "url": "{{base_url}}/trip/1/cancel"
      }
    }
  ]
}
```

## 自动化测试代码示例

### JUnit 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
public class TripPermissionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testCancelTripWithPermission() throws Exception {
        // 用户1参与行程1
        Long userId1 = 1L;
        Long tripId = 1L;
        String token = jwtUtil.generateToken(userId1, "user1");

        mockMvc.perform(put("/api/trip/{tripId}/cancel", tripId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDesc").value("已取消"));
    }

    @Test
    public void testCancelTripWithoutPermission() throws Exception {
        // 用户2未参与行程1
        Long userId2 = 2L;
        Long tripId = 1L;
        String token = jwtUtil.generateToken(userId2, "user2");

        mockMvc.perform(put("/api/trip/{tripId}/cancel", tripId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("您没有权限操作此行程"));
    }

    @Test
    public void testCancelTripWithoutToken() throws Exception {
        Long tripId = 1L;

        mockMvc.perform(put("/api/trip/{tripId}/cancel", tripId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未提供认证令牌"));
    }

    @Test
    public void testCancelTripWithInvalidToken() throws Exception {
        Long tripId = 1L;

        mockMvc.perform(put("/api/trip/{tripId}/cancel", tripId)
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("无效的认证令牌"));
    }
}
```

## 前端测试

### Cypress 测试

```javascript
describe('行程权限验证', () => {
  const user1Token = 'user1_jwt_token'
  const user2Token = 'user2_jwt_token'

  it('有权限的用户可以取消行程', () => {
    cy.request({
      method: 'PUT',
      url: '/api/trip/1/cancel',
      headers: {
        'Authorization': `Bearer ${user1Token}`
      }
    }).then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body.statusDesc).to.eq('已取消')
    })
  })

  it('无权限的用户不能取消行程', () => {
    cy.request({
      method: 'PUT',
      url: '/api/trip/1/cancel',
      headers: {
        'Authorization': `Bearer ${user2Token}`
      },
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.eq(403)
      expect(response.body.message).to.eq('您没有权限操作此行程')
    })
  })

  it('未登录用户不能取消行程', () => {
    cy.request({
      method: 'PUT',
      url: '/api/trip/1/cancel',
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.eq(401)
      expect(response.body.message).to.eq('未提供认证令牌')
    })
  })
})
```

## 安全性检查清单

- [x] 验证 JWT token 有效性
- [x] 验证用户是否参与行程
- [x] 返回正确的 HTTP 状态码
- [x] 提供清晰的错误消息
- [x ] 防止 SQL 注入（使用参数化查询）
- [ ] 防止 CSRF 攻击（如需要）
- [ ] 记录权限失败的审计日志
- [ ] 实现速率限制防止暴力攻击

## 常见问题

### Q: 如果用户曾经参与行程但后来被移除怎么办？

A: 当前实现只检查 `match_record` 表中是否存在记录。如果用户被移除（记录被删除），则无权限。

### Q: 是否需要区分车主和乘客的权限？

A: 当前实现不区分角色，所有参与行程的用户都有相同权限。如需区分，可在 `validateUserPermissionForTrip` 方法中添加角色检查。

### Q: 是否可以撤销已取消的行程？

A: 当前实现允许将状态改为任何有效值，包括从"已取消"改回其他状态。如需限制，需在 `updateTripStatus` 方法中添加状态转换规则验证。
