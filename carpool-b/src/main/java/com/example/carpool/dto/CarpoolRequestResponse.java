package com.example.carpool.dto;

import com.example.carpool.entity.CarpoolRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CarpoolRequestResponse {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Boolean hasCar;
    private Integer maxPassengerCount;
    private Integer passengerCount;
    private String startLocation;
    private Double startLatitude;
    private Double startLongitude;
    private String endLocation;
    private Double endLatitude;
    private Double endLongitude;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earliestDepartureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestDepartureTime;

    private String phoneNumber;
    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public CarpoolRequestResponse() {}

    public CarpoolRequestResponse(CarpoolRequest request) {
        this.id = request.getId();
        this.userId = request.getUserId();
        this.hasCar = request.getHasCar();
        this.maxPassengerCount = request.getMaxPassengerCount();
        this.passengerCount = request.getPassengerCount();
        this.startLocation = request.getStartLocation();
        this.startLatitude = request.getStartLatitude();
        this.startLongitude = request.getStartLongitude();
        this.endLocation = request.getEndLocation();
        this.endLatitude = request.getEndLatitude();
        this.endLongitude = request.getEndLongitude();
        this.earliestDepartureTime = request.getEarliestDepartureTime();
        this.latestDepartureTime = request.getLatestDepartureTime();
        this.phoneNumber = request.getPhoneNumber();
        this.statusDesc = request.getStatusDesc();
        this.createdAt = request.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Boolean getHasCar() {
        return hasCar;
    }

    public void setHasCar(Boolean hasCar) {
        this.hasCar = hasCar;
    }

    public Integer getMaxPassengerCount() {
        return maxPassengerCount;
    }

    public void setMaxPassengerCount(Integer maxPassengerCount) {
        this.maxPassengerCount = maxPassengerCount;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
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

    public LocalDateTime getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public void setEarliestDepartureTime(LocalDateTime earliestDepartureTime) {
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public LocalDateTime getLatestDepartureTime() {
        return latestDepartureTime;
    }

    public void setLatestDepartureTime(LocalDateTime latestDepartureTime) {
        this.latestDepartureTime = latestDepartureTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
