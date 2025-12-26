CREATE TABLE carpool_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    has_car BOOLEAN NOT NULL COMMENT '是否有车',
    passenger_count INT NOT NULL COMMENT '乘客数量',
    max_passenger_count INT NOT NULL COMMENT '最大乘客数量',
    start_location VARCHAR(255) NOT NULL COMMENT '起点位置',
    start_latitude DECIMAL(10, 7) COMMENT '起点纬度',
    start_longitude DECIMAL(10, 7) COMMENT '起点经度',
    end_location VARCHAR(255) NOT NULL COMMENT '终点位置',
    end_latitude DECIMAL(10, 7) COMMENT '终点纬度',
    end_longitude DECIMAL(10, 7) COMMENT '终点经度',
    earliest_departure_time DATETIME NOT NULL COMMENT '最早出发时间',
    latest_departure_time DATETIME NOT NULL COMMENT '最晚出发时间',
    phone_number VARCHAR(20) NOT NULL COMMENT '联系电话',
    status_desc VARCHAR(50) NOT NULL COMMENT '请求状态描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE match_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT '请求ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    trip_id BIGINT NOT NULL COMMENT '行程ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
);

CREATE TABLE trip_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_location VARCHAR(255) NOT NULL COMMENT '起点位置',
    start_latitude DECIMAL(10, 7) COMMENT '起点纬度',
    start_longitude DECIMAL(10, 7) COMMENT '起点经度',
    end_location VARCHAR(255) NOT NULL COMMENT '终点位置',
    end_latitude DECIMAL(10, 7) COMMENT '终点纬度',
    end_longitude DECIMAL(10, 7) COMMENT '终点经度',
    departure_at DATETIME NOT NULL COMMENT '出发时间',
    status_desc VARCHAR(50) NOT NULL COMMENT '行程状态描述',
    passenger_count INT NOT NULL COMMENT '乘客数量',

    match_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    phone_number VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone_number)
) COMMENT='用户表';

-- 拼车邀请表
CREATE TABLE carpool_invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '邀请ID',
    inviter_id BIGINT NOT NULL COMMENT '发起者ID（用户ID）',
    carpool_request_id BIGINT NOT NULL COMMENT '对应的拼车需求ID',
    passenger_count INT NOT NULL COMMENT '发起者人数',
    message VARCHAR(255) COMMENT '留言备注',
    status INT NOT NULL DEFAULT 1 COMMENT '邀请状态：1-待处理，2-已接受，3-已拒绝，4-已取消',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (carpool_request_id) REFERENCES carpool_request(id) ON DELETE CASCADE,
    INDEX idx_inviter (inviter_id),
    INDEX idx_request (carpool_request_id),
    INDEX idx_status (status)
) COMMENT='拼车邀请表';
