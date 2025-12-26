package com.example.carpool.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_record")
public class TripRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_location", nullable = false, length = 255)
    private String startLocation;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "end_location", nullable = false, length = 255)
    private String endLocation;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "departure_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureAt;

    @Column(name = "status_desc", nullable = false, length = 50)
    private String statusDesc;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "match_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime matchAt;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        matchAt = LocalDateTime.now();
    }

    public TripRecord() {}

    public TripRecord(String startLocation, Double startLatitude, Double startLongitude,
                      String endLocation, Double endLatitude, Double endLongitude,
                      LocalDateTime departureAt, String statusDesc, Integer passengerCount) {
        this.startLocation = startLocation;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLocation = endLocation;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.departureAt = departureAt;
        this.statusDesc = statusDesc;
        this.passengerCount = passengerCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public LocalDateTime getDepartureAt() {
        return departureAt;
    }

    public void setDepartureAt(LocalDateTime departureAt) {
        this.departureAt = departureAt;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public LocalDateTime getMatchAt() {
        return matchAt;
    }

    public void setMatchAt(LocalDateTime matchAt) {
        this.matchAt = matchAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TripRecord{" +
                "id=" + id +
                ", startLocation='" + startLocation + '\'' +
                ", startLatitude=" + startLatitude +
                ", startLongitude=" + startLongitude +
                ", endLocation='" + endLocation + '\'' +
                ", endLatitude=" + endLatitude +
                ", endLongitude=" + endLongitude +
                ", departureAt=" + departureAt +
                ", statusDesc='" + statusDesc + '\'' +
                ", passengerCount=" + passengerCount +
                ", matchAt=" + matchAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
