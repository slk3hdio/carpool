# 使用 Python 3.11-slim 基础镜像
FROM python:3.11-slim

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        gcc \
        default-libmysqlclient-dev \
        pkg-config \
    && rm -rf /var/lib/apt/lists/*

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制依赖文件
COPY requirements.txt .

# 安装 Python 依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制脚本文件
COPY run.py .
COPY config.json .

# 创建非 root 用户
RUN addgroup --system --gid 1000 python && \
    adduser --system --uid 1000 --gid 1000 python && \
    chown -R python:python /app

# 切换到非 root 用户
USER python

# 健康检查（检查进程是否运行）
HEALTHCHECK --interval=60s --timeout=10s --start-period=30s --retries=3 \
    CMD pgrep -f "python.*run.py" || exit 1

# 运行脚本
CMD ["python", "-u", "run.py"]
