-- 历史路况查询数据库索引优化脚本
-- 用于提高历史数据查询性能

-- ========================================
-- road_traffic_overall 表索引优化
-- ========================================

-- 1. 复合索引用于历史数据查询 (最重要的索引)
-- 用于根据道路名称、城市和时间范围查询历史数据
CREATE INDEX idx_road_city_time_historical ON road_traffic_overall (road_name, city, request_time DESC);

-- 2. 城市单列索引
-- 用于按城市查询和数据统计
CREATE INDEX idx_city_query ON road_traffic_overall (city);

-- 3. 道路名称单列索引
-- 用于按道路名称查询
CREATE INDEX idx_road_name_query ON road_traffic_overall (road_name);

-- 4. 时间索引用于时间范围查询
-- 用于按时间范围筛选数据
CREATE INDEX idx_request_time_query ON road_traffic_overall (request_time DESC);

-- 5. 拥堵状态索引
-- 用于按拥堵状态查询和统计
CREATE INDEX idx_evaluation_status ON road_traffic_overall (evaluation_status);

-- 6. 唯一索引用于获取每个道路的最新数据
-- 配合子查询提高性能
CREATE INDEX id_composite_for_latest ON road_traffic_overall (road_name, city, request_time DESC, id);

-- ========================================
-- congestion_sections 表索引优化
-- ========================================

-- 7. overall_id 索引
-- 用于快速关联查询路况的详细拥堵信息
CREATE INDEX idx_overall_id_lookup ON congestion_sections (overall_id);

-- 8. overall_id 和创建时间复合索引
-- 用于获取路况最新的拥堵路段信息
CREATE INDEX idx_overall_id_created_desc ON congestion_sections (overall_id, created_at DESC);

-- 9. 速度索引用于统计计算
-- 用于平均速度等统计查询
CREATE INDEX idx_speed_for_stats ON congestion_sections (speed) WHERE speed IS NOT NULL;

-- 10. 拥堵距离索引
-- 用于拥堵距离统计
CREATE INDEX idx_congestion_distance_stats ON congestion_sections (congestion_distance) WHERE congestion_distance IS NOT NULL;

-- ========================================
-- 分析表以更新索引统计信息
-- ========================================

-- 分析 road_traffic_overall 表
ANALYZE TABLE road_traffic_overall;

-- 分析 congestion_sections 表
ANALYZE TABLE congestion_sections;

-- ========================================
-- 查询优化建议
-- ========================================

/*
使用这些索引的查询示例：

1. 历史数据查询（使用索引1）:
   SELECT * FROM road_traffic_overall
   WHERE road_name = '长安街' AND city = '北京'
   AND request_time BETWEEN '2024-12-20 08:00:00' AND '2024-12-21 08:00:00'
   ORDER BY request_time DESC;

2. 城市列表查询（使用索引2）:
   SELECT DISTINCT city FROM road_traffic_overall;

3. 城市道路列表查询（使用索引3）:
   SELECT DISTINCT road_name FROM road_traffic_overall WHERE city = '北京';

4. 拥堵统计查询（使用索引5）:
   SELECT evaluation_status, COUNT(*) FROM road_traffic_overall
   WHERE request_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
   GROUP BY evaluation_status;

5. 最新路况查询（使用索引6）:
   SELECT * FROM road_traffic_overall r1
   WHERE r1.id = (
     SELECT MAX(r2.id)
     FROM road_traffic_overall r2
     WHERE r2.road_name = r1.road_name AND r2.city = r1.city
   );

6. 拥堵路段查询（使用索引8）:
   SELECT * FROM congestion_sections
   WHERE overall_id = 12345
   ORDER BY created_at DESC;

7. 平均速度计算（使用索引9）:
   SELECT AVG(speed) FROM congestion_sections
   WHERE overall_id = 12345 AND speed IS NOT NULL;

8. 总拥堵距离（使用索引10）:
   SELECT SUM(congestion_distance) FROM congestion_sections
   WHERE overall_id = 12345 AND congestion_distance IS NOT NULL;
*/

-- ========================================
-- 性能监控查询
-- ========================================

-- 查看索引使用情况
-- SELECT
--   TABLE_NAME,
--   INDEX_NAME,
--   CARDINALITY,
--   SUB_PART,
--   NULLABLE
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME IN ('road_traffic_overall', 'congestion_sections')
-- ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 查看查询执行计划（示例）
-- EXPLAIN SELECT * FROM road_traffic_overall
-- WHERE road_name = '长安街' AND city = '北京'
-- AND request_time BETWEEN '2024-12-20 08:00:00' AND '2024-12-21 08:00:00'
-- ORDER BY request_time DESC;