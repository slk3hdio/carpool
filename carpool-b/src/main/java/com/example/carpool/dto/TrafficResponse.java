package com.example.carpool.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TrafficResponse {
    private Long id;
    private String roadName;
    private String city;
    private Integer evaluationStatus;
    private String evaluationStatusDesc;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    // 从拥堵路段计算得出的字段
    private Double speed;
    private Integer congestionDistance;
    private String statusText;

    // 默认构造函数
    public TrafficResponse() {}

    // 带参构造函数
    public TrafficResponse(Long id, String roadName, String city, Integer evaluationStatus, String description, LocalDateTime requestTime) {
        this.id = id;
        this.roadName = roadName;
        this.city = city;
        this.evaluationStatus = evaluationStatus;
        this.description = description;
        this.requestTime = requestTime;
        this.statusText = getStatusText(evaluationStatus);
    }

    // 根据evaluation_status获取状态文本
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未知";
            case 1: return "畅通";
            case 2: return "缓行";
            case 3: return "拥堵";
            case 4: return "严重拥堵";
            default: return "未知";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(Integer evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
        this.statusText = getStatusText(evaluationStatus);
    }

    public String getEvaluationStatusDesc() {
        return evaluationStatusDesc;
    }

    public void setEvaluationStatusDesc(String evaluationStatusDesc) {
        this.evaluationStatusDesc = evaluationStatusDesc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Integer getCongestionDistance() {
        return congestionDistance;
    }

    public void setCongestionDistance(Integer congestionDistance) {
        this.congestionDistance = congestionDistance;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}