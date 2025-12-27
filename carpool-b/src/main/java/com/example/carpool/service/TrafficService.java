package com.example.carpool.service;

import com.example.carpool.dto.TrafficResponse;
import com.example.carpool.dto.TrafficStatsResponse;
import com.example.carpool.entity.CongestionSection;
import com.example.carpool.entity.RoadTrafficOverall;
import com.example.carpool.repository.CongestionSectionRepository;
import com.example.carpool.repository.RoadTrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TrafficService {

    private static final Logger logger = LoggerFactory.getLogger(TrafficService.class);

    @Autowired
    private RoadTrafficRepository roadTrafficRepository;

    @Autowired
    private CongestionSectionRepository congestionSectionRepository;

    /**
     * 获取所有道路的最新路况信息
     */
    public Page<TrafficResponse> getAllLatestTraffic(Pageable pageable) {
        Page<RoadTrafficOverall> trafficPage = roadTrafficRepository.findLatestForEachRoad(pageable);
        return convertToTrafficResponsePage(trafficPage);
    }

    /**
     * 根据城市获取路况信息
     */
    public Page<TrafficResponse> getTrafficByCity(String city, Pageable pageable) {
        Page<RoadTrafficOverall> trafficPage = roadTrafficRepository.findByCityOrderByRequestTimeDesc(city, pageable);
        return convertToTrafficResponsePage(trafficPage);
    }

    /**
     * 根据道路名称和城市查询路况
     */
    public Page<TrafficResponse> getTrafficByRoadAndCity(String roadName, String city, Pageable pageable) {
        Page<RoadTrafficOverall> trafficPage = roadTrafficRepository.findLatestByRoadAndCity(roadName, city, pageable);
        return convertToTrafficResponsePage(trafficPage);
    }

    /**
     * 根据拥堵状态查询路况
     */
    public List<TrafficResponse> getTrafficByStatus(Integer status) {
        List<RoadTrafficOverall> trafficList = roadTrafficRepository.findByEvaluationStatusOrderByRequestTimeDesc(status);
        return convertToTrafficResponseList(trafficList);
    }

    /**
     * 根据关键字搜索路况
     */
    public List<TrafficResponse> searchTraffic(String keyword) {
        List<RoadTrafficOverall> trafficList = roadTrafficRepository.findByKeyword(keyword);
        return convertToTrafficResponseList(trafficList);
    }

    /**
     * 获取路况统计信息
     */
    public TrafficStatsResponse getTrafficStats() {
        LocalDateTime since = LocalDateTime.now().minusHours(24); // 获取最近24小时的统计

        List<Object[]> stats = roadTrafficRepository.getTrafficStatsSince(since);
        Map<String, Long> statusDistribution = new HashMap<>();
        long total = 0;
        long smooth = 0;
        long slow = 0;
        long congested = 0;
        long heavy = 0;

        for (Object[] stat : stats) {
            Integer status = (Integer) stat[0];
            Long count = (Long) stat[1];
            total += count;

            String statusKey = getStatusKey(status);
            statusDistribution.put(statusKey, count);

            switch (status) {
                case 0:
                case 1:
                    smooth += count;
                    break;
                case 2:
                    slow += count;
                    break;
                case 3:
                    congested += count;
                    break;
                case 4:
                    heavy += count;
                    break;
            }
        }

        TrafficStatsResponse response = new TrafficStatsResponse(total, smooth, slow, congested, heavy);
        response.setStatusDistribution(statusDistribution);
        return response;
    }

    /**
     * 获取路况详情（包含拥堵路段信息）
     */
    public TrafficResponse getTrafficDetails(Long id) {
        RoadTrafficOverall traffic = roadTrafficRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路况信息不存在: " + id));

        TrafficResponse response = convertToTrafficResponse(traffic);

        // 获取拥堵路段的额外信息
        List<CongestionSection> sections = congestionSectionRepository.findByOverallIdOrderByCreatedAtDesc(id);
        if (!sections.isEmpty()) {
            // 计算平均速度
            Double avgSpeed = congestionSectionRepository.getAverageSpeedByOverallId(id);
            if (avgSpeed != null) {
                response.setSpeed(avgSpeed);
            }

            // 获取总拥堵距离
            Integer totalDistance = congestionSectionRepository.getTotalCongestionDistance(id);
            if (totalDistance != null && totalDistance > 0) {
                response.setCongestionDistance(totalDistance / 1000); // 转换为公里
            }
        }

        return response;
    }

    /**
     * 转换RoadTrafficOverall为TrafficResponse
     */
    private TrafficResponse convertToTrafficResponse(RoadTrafficOverall traffic) {
        return new TrafficResponse(
            traffic.getId(),
            traffic.getRoadName(),
            traffic.getCity(),
            traffic.getEvaluationStatus(),
            traffic.getDescription(),
            traffic.getRequestTime()
        );
    }

    /**
     * 批量转换RoadTrafficOverall为TrafficResponse
     */
    private List<TrafficResponse> convertToTrafficResponseList(List<RoadTrafficOverall> trafficList) {
        return trafficList.stream()
                .map(this::convertToTrafficResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换Page<RoadTrafficOverall>为Page<TrafficResponse>
     */
    private Page<TrafficResponse> convertToTrafficResponsePage(Page<RoadTrafficOverall> trafficPage) {
        List<TrafficResponse> responseList = convertToTrafficResponseList(trafficPage.getContent());
        return new PageImpl<>(responseList, trafficPage.getPageable(), trafficPage.getTotalElements());
    }

    /**
     * 根据状态码获取状态键
     */
    private String getStatusKey(Integer status) {
        if (status == null) return "unknown";
        switch (status) {
            case 0: return "畅通";
            case 1: return "畅通";
            case 2: return "缓行";
            case 3: return "拥堵";
            case 4: return "严重拥堵";
            default: return "未知";
        }
    }

    // ========== 历史数据查询相关方法 ==========

    /**
     * 获取指定道路在指定时间范围内的历史数据
     */
    public Page<TrafficResponse> getHistoricalTraffic(
            String roadName, String city,
            LocalDateTime startTime, LocalDateTime endTime,
            Pageable pageable) {

        logger.info("========== 开始查询历史路况数据 ==========");
        logger.info("查询参数 - 道路名称: {}, 城市名称: {}, 开始时间: {}, 结束时间: {}",
                roadName, city, startTime, endTime);
        logger.info("分页参数 - 页码: {}, 每页大小: {}, 排序: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        // 参数验证
        logger.debug("步骤1: 验证查询参数...");
        validateHistoricalQueryParams(roadName, city, startTime, endTime);
        logger.debug("步骤1完成: 参数验证通过 ✓");

        // 检查时间范围是否超过限制
        logger.debug("步骤2: 检查时间范围...");
        long hoursBetween = ChronoUnit.HOURS.between(startTime, endTime);
        logger.debug("查询时间跨度: {} 小时 (约 {} 天)", hoursBetween, String.format("%.1f", hoursBetween / 24.0));
        if (hoursBetween > 30 * 24) { // 30天限制
            logger.warn("查询时间范围超过30天限制：{}小时", hoursBetween);
            throw new IllegalArgumentException("查询时间范围不能超过30天");
        }
        logger.debug("步骤2完成: 时间范围检查通过 ✓");

        // 直接使用原始城市名称和道路名称（不做任何标准化处理）
        logger.debug("步骤3: 准备查询参数...");
        String trimmedCity = city.trim();
        String trimmedRoadName = roadName.trim();
        logger.info("最终查询参数 - 城市名称: [{}], 道路名称: [{}]", trimmedCity, trimmedRoadName);
        logger.debug("步骤3完成: 查询参数准备完成 ✓");

        // 检查数据量是否过大
        logger.debug("步骤4: 检查数据量...");
        Long dataCount = roadTrafficRepository.countHistoricalTraffic(trimmedRoadName, trimmedCity, startTime, endTime);
        logger.info("数据库中符合条件的数据总量: {} 条", dataCount);
        if (dataCount > 5000) { // 单次查询最多5000条数据
            logger.warn("查询数据量过大：{}条，超过5000条限制", dataCount);
            throw new IllegalArgumentException("查询数据量过大，请缩小时间范围");
        }
        logger.debug("步骤4完成: 数据量检查通过 ✓");

        if (dataCount == 0) {
            logger.warn("警告: 数据库中没有符合条件的历史记录！");
            logger.warn("提示: 请检查城市名称和道路名称是否正确，以及数据库中是否有该时间段的数据");
        }

        // 查询历史数据
        logger.debug("步骤5: 执行数据库查询...");
        long queryStartTime = System.currentTimeMillis();
        Page<RoadTrafficOverall> trafficPage = roadTrafficRepository.findHistoricalTraffic(
                trimmedRoadName, trimmedCity, startTime, endTime, pageable);
        long queryEndTime = System.currentTimeMillis();
        logger.info("步骤5完成: 数据库查询完成 ✓ (耗时: {} ms)", queryEndTime - queryStartTime);
        logger.info("查询返回数据量: {} 条, 总记录数: {} 条, 总页数: {} 页",
                trafficPage.getContent().size(), trafficPage.getTotalElements(), trafficPage.getTotalPages());

        // 为每条历史数据补充速度信息
        logger.debug("步骤6: 补充速度和拥堵距离信息...");
        long enrichStartTime = System.currentTimeMillis();
        List<TrafficResponse> responses = trafficPage.getContent().stream()
                .map(traffic -> {
                    TrafficResponse response = convertToTrafficResponse(traffic);
                    // 获取该时间点的平均速度
                    Double avgSpeed = getAverageSpeedForTraffic(traffic);
                    if (avgSpeed != null) {
                        response.setSpeed(avgSpeed);
                    }
                    // 获取拥堵距离
                    Integer congestionDistance = getCongestionDistanceForTraffic(traffic);
                    if (congestionDistance != null) {
                        response.setCongestionDistance(congestionDistance);
                    }
                    return response;
                })
                .collect(Collectors.toList());
        long enrichEndTime = System.currentTimeMillis();
        logger.debug("步骤6完成: 数据信息补充完成 ✓ (耗时: {} ms)", enrichEndTime - enrichStartTime);

        // 记录查询结果统计
        if (!responses.isEmpty()) {
            logger.info("========== 查询结果统计 ==========");
            logger.info("返回结果数: {} 条", responses.size());
            logger.info("总记录数: {} 条", trafficPage.getTotalElements());
            logger.info("当前页: {}/{}, 每页大小: {} 条",
                    trafficPage.getPageable().getPageNumber() + 1,
                    trafficPage.getTotalPages(),
                    trafficPage.getPageable().getPageSize());

            // 统计各状态的数量
            Map<Integer, Long> statusStats = responses.stream()
                    .collect(Collectors.groupingBy(
                            TrafficResponse::getEvaluationStatus,
                            Collectors.counting()
                    ));
            logger.info("状态分布: {}", statusStats);
            logger.info("==================================");
        } else {
            logger.warn("查询结果为空，未返回任何历史数据");
        }

        logger.info("========== 历史路况数据查询完成 ==========");

        return new PageImpl<>(responses, trafficPage.getPageable(), trafficPage.getTotalElements());
    }

    /**
     * 获取所有支持的城市列表
     */
    public List<String> getSupportedCities() {
        return roadTrafficRepository.findDistinctCities();
    }

    /**
     * 获取指定城市的所有道路列表
     */
    public List<String> getRoadsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市名称不能为空");
        }

        // 直接使用原始城市名称（不做标准化处理）
        String trimmedCity = city.trim();
        List<String> roads = roadTrafficRepository.findDistinctRoadsByCity(trimmedCity);

        if (roads.isEmpty()) {
            throw new IllegalArgumentException("城市 '" + city + "' 暂无数据或城市名称错误");
        }
        return roads;
    }

    /**
     * 验证历史数据查询参数
     */
    private void validateHistoricalQueryParams(String roadName, String city, LocalDateTime startTime, LocalDateTime endTime) {
        if (roadName == null || roadName.trim().isEmpty()) {
            throw new IllegalArgumentException("道路名称不能为空");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市名称不能为空");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }
        if (endTime.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("结束时间不能超过当前时间");
        }

        // 直接使用原始城市名称和道路名称（不做标准化处理）
        String trimmedCity = city.trim();
        String trimmedRoadName = roadName.trim();

        // 检查道路是否存在
        if (!roadTrafficRepository.existsByRoadNameAndCity(trimmedRoadName, trimmedCity)) {
            throw new IllegalArgumentException("道路 '" + roadName + "' 在城市 '" + city + "' 中不存在");
        }
    }

    /**
     * 标准化城市名称，移除常见的后缀
     */
    private String normalizeCityName(String cityName) {
        if (cityName == null) return null;

        // 移除常见的城市后缀
        if (cityName.endsWith("市")) {
            return cityName.substring(0, cityName.length() - 1);
        }
        if (cityName.endsWith("自治区")) {
            return cityName.substring(0, cityName.length() - 3);
        }
        if (cityName.endsWith("自治州")) {
            return cityName.substring(0, cityName.length() - 3);
        }
        if (cityName.endsWith("地区")) {
            return cityName.substring(0, cityName.length() - 2);
        }

        return cityName;
    }

    /**
     * 获取指定路况记录的平均速度
     */
    private Double getAverageSpeedForTraffic(RoadTrafficOverall traffic) {
        List<CongestionSection> sections = congestionSectionRepository.findByOverallIdOrderByCreatedAtDesc(traffic.getId());
        if (sections.isEmpty()) {
            return null;
        }

        java.util.OptionalDouble avgSpeed = sections.stream()
                .filter(section -> section.getSpeed() != null)
                .mapToDouble(section -> section.getSpeed().doubleValue())
                .average();

        return avgSpeed.isPresent() ? avgSpeed.getAsDouble() : null;
    }

    /**
     * 获取指定路况记录的拥堵距离
     */
    private Integer getCongestionDistanceForTraffic(RoadTrafficOverall traffic) {
        Integer totalDistance = congestionSectionRepository.getTotalCongestionDistance(traffic.getId());
        return (totalDistance != null && totalDistance > 0) ? totalDistance / 1000 : null; // 转换为公里
    }
}