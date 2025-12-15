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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TrafficService {

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
}