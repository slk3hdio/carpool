# 使用多阶段构建优化镜像大小

# 阶段 1: 构建阶段
FROM gradle:8.11-jdk21 AS builder

# 设置工作目录
WORKDIR /app

# 复制构建文件和源代码
COPY build.gradle gradlew ./
COPY src ./src
COPY gradle ./gradle

# 使用 Gradle 构建应用
RUN chmod +x gradlew && \
    ./gradlew build --no-daemon

# 阶段 2: 运行阶段
FROM eclipse-temurin:21-jre-alpine

# 设置时区为上海
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用目录和用户
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/build/libs/*.jar app.jar

# 修改文件权限
RUN chown -R spring:spring /app

# 切换到非 root 用户
USER spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=docker", \
    "-jar", \
    "app.jar"]
