# 使用多阶段构建优化镜像大小

# 阶段 1: 构建阶段
FROM node:22-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 package 文件
COPY package.json package-lock.json ./

# 安装依赖（包括开发依赖，因为构建需要）
RUN npm ci

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 阶段 2: 运行阶段 (使用 nginx)
FROM nginx:alpine

# 安装时区数据和设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 删除默认的 nginx 配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
