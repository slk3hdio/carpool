package com.example.carpool.controller;

import com.example.carpool.dto.TrafficResponse;
import com.example.carpool.dto.TrafficStatsResponse;
import com.example.carpool.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/traffic")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175","http://localhost:5176","http://localhost:8080"})
public class TrafficController {

    private static final Logger logger = LoggerFactory.getLogger(TrafficController.class);

    @Autowired
    private TrafficService trafficService;

    /**
     * 获取所有道路的最新路况信息
     */
    @GetMapping
    public ResponseEntity<Page<TrafficResponse>> getAllTraffic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "requestTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TrafficResponse> traffic = trafficService.getAllLatestTraffic(pageable);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 根据城市获取路况信息
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<Page<TrafficResponse>> getTrafficByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestTime").descending());
        Page<TrafficResponse> traffic = trafficService.getTrafficByCity(city, pageable);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 根据道路和城市查询路况
     */
    @GetMapping("/road/{roadName}/city/{city}")
    public ResponseEntity<Page<TrafficResponse>> getTrafficByRoadAndCity(
            @PathVariable String roadName,
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestTime").descending());
        Page<TrafficResponse> traffic = trafficService.getTrafficByRoadAndCity(roadName, city, pageable);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 根据拥堵状态查询路况
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrafficResponse>> getTrafficByStatus(@PathVariable Integer status) {
        List<TrafficResponse> traffic = trafficService.getTrafficByStatus(status);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 搜索路况信息
     */
    @GetMapping("/search")
    public ResponseEntity<List<TrafficResponse>> searchTraffic(
            @RequestParam String keyword) {
        List<TrafficResponse> traffic = trafficService.searchTraffic(keyword);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 获取路况统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<TrafficStatsResponse> getTrafficStats() {
        TrafficStatsResponse stats = trafficService.getTrafficStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取路况详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrafficResponse> getTrafficDetails(@PathVariable Long id) {
        TrafficResponse traffic = trafficService.getTrafficDetails(id);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 获取路况概览数据（用于首页展示）
     */
    @GetMapping("/overview")
    public ResponseEntity<Page<TrafficResponse>> getTrafficOverview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestTime").descending());
        Page<TrafficResponse> traffic = trafficService.getAllLatestTraffic(pageable);
        return ResponseEntity.ok(traffic);
    }

    /**
     * 获取热门道路路况
     */
    @GetMapping("/popular")
    public ResponseEntity<List<TrafficResponse>> getPopularTraffic() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("requestTime").descending());
        Page<TrafficResponse> traffic = trafficService.getAllLatestTraffic(pageable);
        return ResponseEntity.ok(traffic.getContent());
    }

    // ========== 历史数据查询相关接口 ==========

    /**
     * 获取历史路况数据
     */
    @GetMapping("/historical")
    public ResponseEntity<Page<TrafficResponse>> getHistoricalTraffic(
            @RequestParam String roadName,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        long requestStartTime = System.currentTimeMillis();
        logger.info("========== 收到历史路况查询请求 ==========");
        logger.info("请求参数 - 道路名称: [{}], 城市: [{}], 开始时间: [{}], 结束时间: [{}]",
                roadName, city, startTime, endTime);
        logger.info("分页参数 - 页码: {}, 每页大小: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("requestTime").ascending());
            logger.debug("分页对象创建完成: {}", pageable);

            Page<TrafficResponse> historicalData = trafficService.getHistoricalTraffic(
                    roadName, city, startTime, endTime, pageable);

            long requestEndTime = System.currentTimeMillis();
            logger.info("请求处理完成 - 返回 {} 条记录, 总耗时: {} ms",
                    historicalData.getContent().size(), requestEndTime - requestStartTime);
            logger.info("========== 历史路况查询请求处理完成 ==========");

            return ResponseEntity.ok(historicalData);
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            throw new RuntimeException(e.getMessage()); // 会被全局异常处理器处理
        } catch (Exception e) {
            logger.error("查询历史路况数据时发生异常", e);
            throw new RuntimeException("查询历史路况数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取城市道路列表
     */
    @GetMapping("/cities/{city}/roads")
    public ResponseEntity<List<String>> getRoadsByCity(@PathVariable String city) {
        logger.info("收到获取城市道路列表请求 - 城市: [{}]", city);

        try {
            List<String> roads = trafficService.getRoadsByCity(city);
            logger.info("查询成功 - 找到 {} 条道路", roads.size());
            return ResponseEntity.ok(roads);
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            logger.error("获取城市道路列表时发生异常 - 城市: [{}]", city, e);
            throw new RuntimeException("获取城市道路列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的城市列表
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getSupportedCities() {
        logger.info("收到获取支持城市列表请求");

        try {
            List<String> cities = trafficService.getSupportedCities();
            logger.info("查询成功 - 找到 {} 个城市", cities.size());
            logger.debug("城市列表: {}", cities);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            logger.error("获取支持城市列表时发生异常", e);
            throw new RuntimeException("获取支持城市列表失败: " + e.getMessage());
        }
    }
}