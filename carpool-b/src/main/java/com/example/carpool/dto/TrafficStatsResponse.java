package com.example.carpool.dto;

import java.util.Map;

public class TrafficStatsResponse {
    private Long totalRoads;
    private Long smoothRoads;
    private Long slowRoads;
    private Long congestedRoads;
    private Long heavyRoads;
    private Map<String, Long> statusDistribution;

    // 默认构造函数
    public TrafficStatsResponse() {}

    // 带参构造函数
    public TrafficStatsResponse(Long totalRoads, Long smoothRoads, Long slowRoads, Long congestedRoads, Long heavyRoads) {
        this.totalRoads = totalRoads;
        this.smoothRoads = smoothRoads;
        this.slowRoads = slowRoads;
        this.congestedRoads = congestedRoads;
        this.heavyRoads = heavyRoads;
    }

    // Getters and Setters
    public Long getTotalRoads() {
        return totalRoads;
    }

    public void setTotalRoads(Long totalRoads) {
        this.totalRoads = totalRoads;
    }

    public Long getSmoothRoads() {
        return smoothRoads;
    }

    public void setSmoothRoads(Long smoothRoads) {
        this.smoothRoads = smoothRoads;
    }

    public Long getSlowRoads() {
        return slowRoads;
    }

    public void setSlowRoads(Long slowRoads) {
        this.slowRoads = slowRoads;
    }

    public Long getCongestedRoads() {
        return congestedRoads;
    }

    public void setCongestedRoads(Long congestedRoads) {
        this.congestedRoads = congestedRoads;
    }

    public Long getHeavyRoads() {
        return heavyRoads;
    }

    public void setHeavyRoads(Long heavyRoads) {
        this.heavyRoads = heavyRoads;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }
}