# 后端依赖配置说明

## 已添加的依赖

### 1. JWT (JSON Web Token) 依赖
```gradle
// JWT 依赖
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

**用途**：用于生成和验证用户认证令牌
- `jjwt-api`: JWT API接口
- `jjwt-impl`: JWT实现
- `jjwt-jackson`: Jackson集成，用于JSON序列化

### 2. BCrypt 密码加密依赖
```gradle
// BCrypt 密码加密
implementation 'org.springframework.security:spring-security-crypto:6.2.1'
implementation 'org.bouncycastle:bcprov-jdk18on:1.77'
```

**用途**：用于安全的密码加密和验证
- `spring-security-crypto`: Spring Security加密模块，提供BCryptPasswordEncoder
- `bcprov-jdk18on`: Bouncy Castle加密库，Java 21版本

## 密码加密改进

### 之前（不安全）
```java
private String encryptPassword(String password) {
    // 简单的Base64编码，不安全！
    return java.util.Base64.getEncoder().encodeToString(password.getBytes());
}
```

### 现在（安全）
```java
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// 注册时加密
user.setPassword(passwordEncoder.encode(request.getPassword()));

// 登录时验证
if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new RuntimeException("用户名或密码错误");
}
```

## BCrypt 优势

1. **单向加密**：密码只能加密，不能解密
2. **自动加盐**：每次加密都自动生成不同的盐值
3. **可调强度**：可以通过调整强度参数（默认10）来增加安全性
4. **时间恒定**：验证时间不会因为密码相似度而泄露信息

## 完整依赖列表

### 核心依赖
- Spring Boot Web (Web框架)
- Spring Boot Data JPA (ORM)
- Spring Boot Validation (数据验证)
- MySQL Connector (数据库驱动)
- Jackson Databind (JSON序列化)

### 认证授权
- JWT (JWT Token生成和验证)
- Spring Security Crypto (BCrypt密码加密)
- Bouncy Castle (加密提供者)

### 实时通信
- Spring WebSocket (WebSocket支持)

### 大数据处理
- Apache Flink (流处理)

### 开发工具
- Spring Boot DevTools (热重载)
- Spring Boot Test (测试框架)

## 依赖版本

- Spring Boot: 3.5.7
- Java: 21
- JWT: 0.12.3
- Spring Security Crypto: 6.2.1
- Bouncy Castle: 1.77
- MySQL Connector: 8.0.33
- Flink: 1.16.3

## 刷新依赖

如果需要重新下载依赖，运行：

```bash
cd carpool-b
./gradlew clean build
```

或者仅刷新依赖：

```bash
./gradlew build --refresh-dependencies
```

## 安全建议

1. ✅ 已使用 BCrypt 加密密码
2. ⚠️ JWT 密钥目前硬编码在代码中，建议移到配置文件
3. ⚠️ 建议添加密码强度验证
4. ⚠️ 建议添加登录失败次数限制
5. ⚠️ 建议启用 HTTPS

## 下一步优化建议

1. 将JWT密钥配置移到 application.properties
2. 添加刷新Token机制
3. 实现记住登录功能
4. 添加邮箱验证
5. 添加密码重置功能
