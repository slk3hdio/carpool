package com.example.carpool.dto;

public class TripStatusUpdateRequest {
    private String statusDesc;

    public TripStatusUpdateRequest() {}

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
}
