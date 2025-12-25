CREATE TABLE carpool_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    has_car BOOLEAN NOT NULL COMMENT '是否有车',
    passenger_count INT NOT NULL COMMENT '乘客数量',
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
    trip_id BIGINT NOT NULL COMMENT '行程ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
)

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
)


