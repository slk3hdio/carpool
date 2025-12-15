-- 主表：存储每次请求的基本信息和整体路况评估
CREATE TABLE road_traffic_overall (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    road_name VARCHAR(100) NOT NULL COMMENT '道路名称',
    city VARCHAR(50) NOT NULL COMMENT '城市名称',
    api_status INT COMMENT 'API状态码',
    message VARCHAR(255) COMMENT 'API响应信息',
    description TEXT COMMENT '路况语义化描述',
    evaluation_status INT COMMENT '路况整体评价(0:未知 1:畅通 2:缓行 3:拥堵 4:严重拥堵)',
    evaluation_status_desc VARCHAR(50) COMMENT '路况整体评价描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 详细路况表：存储具体的拥堵路段信息
CREATE TABLE congestion_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    overall_id BIGINT NOT NULL COMMENT '关联主表ID',
    road_name VARCHAR(100) COMMENT '道路名称',
    section_desc VARCHAR(500) COMMENT '路段拥堵语义化描述',
    status INT COMMENT '路段拥堵评价(0-4)',
    status_desc VARCHAR(50) COMMENT '路段拥堵评价描述',
    speed DECIMAL(5,2) COMMENT '平均通行速度(km/h)',
    congestion_distance INT COMMENT '拥堵距离(米)',
    congestion_trend VARCHAR(20) COMMENT '拥堵趋势(持平/缓解/加重)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (overall_id) REFERENCES road_traffic_overall(id)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_road_city ON road_traffic_overall(road_name, city);
CREATE INDEX idx_request_time ON road_traffic_overall(request_time);
CREATE INDEX idx_overall_id ON congestion_sections(overall_id);