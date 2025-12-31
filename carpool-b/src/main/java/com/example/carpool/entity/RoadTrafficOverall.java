package com.example.carpool.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "road_traffic_overall")
public class RoadTrafficOverall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    @Column(name = "road_name", nullable = false, length = 100)
    private String roadName;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "api_status")
    private Integer apiStatus;

    @Column(name = "message", length = 255)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evaluation_status")
    private Integer evaluationStatus;

    @Column(name = "evaluation_status_desc", length = 50)
    private String evaluationStatusDesc;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // JPA生命周期回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }

    // 默认构造函数
    public RoadTrafficOverall() {}

    // 带参构造函数
    public RoadTrafficOverall(String roadName, String city, Integer evaluationStatus, String description) {
        this.roadName = roadName;
        this.city = city;
        this.evaluationStatus = evaluationStatus;
        this.description = description;
        this.requestTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
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

    public Integer getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(Integer apiStatus) {
        this.apiStatus = apiStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(Integer evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }

    public String getEvaluationStatusDesc() {
        return evaluationStatusDesc;
    }

    public void setEvaluationStatusDesc(String evaluationStatusDesc) {
        this.evaluationStatusDesc = evaluationStatusDesc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "RoadTrafficOverall{" +
                "id=" + id +
                ", requestTime=" + requestTime +
                ", roadName='" + roadName + '\'' +
                ", city='" + city + '\'' +
                ", evaluationStatus=" + evaluationStatus +
                ", description='" + description + '\'' +
                '}';
    }
}