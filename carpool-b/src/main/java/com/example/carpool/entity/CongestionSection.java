package com.example.carpool.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "congestion_sections")
public class CongestionSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "overall_id", nullable = false)
    private Long overallId;

    @Column(name = "road_name", length = 100)
    private String roadName;

    @Column(name = "section_desc", length = 500)
    private String sectionDesc;

    @Column(name = "status")
    private Integer status;

    @Column(name = "status_desc", length = 50)
    private String statusDesc;

    @Column(name = "speed", precision = 5, scale = 2)
    private BigDecimal speed;

    @Column(name = "congestion_distance")
    private Integer congestionDistance;

    @Column(name = "congestion_trend", length = 20)
    private String congestionTrend;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // JPA生命周期回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 默认构造函数
    public CongestionSection() {}

    // 带参构造函数
    public CongestionSection(Long overallId, String roadName, Integer status, BigDecimal speed) {
        this.overallId = overallId;
        this.roadName = roadName;
        this.status = status;
        this.speed = speed;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOverallId() {
        return overallId;
    }

    public void setOverallId(Long overallId) {
        this.overallId = overallId;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getSectionDesc() {
        return sectionDesc;
    }

    public void setSectionDesc(String sectionDesc) {
        this.sectionDesc = sectionDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public Integer getCongestionDistance() {
        return congestionDistance;
    }

    public void setCongestionDistance(Integer congestionDistance) {
        this.congestionDistance = congestionDistance;
    }

    public String getCongestionTrend() {
        return congestionTrend;
    }

    public void setCongestionTrend(String congestionTrend) {
        this.congestionTrend = congestionTrend;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CongestionSection{" +
                "id=" + id +
                ", overallId=" + overallId +
                ", roadName='" + roadName + '\'' +
                ", status=" + status +
                ", speed=" + speed +
                ", congestionDistance=" + congestionDistance +
                '}';
    }
}