package com.example.carpool.dto;

public class InvitationRequest {
    private Long inviterId;
    private Long carpoolRequestId;
    private Integer passengerCount;
    private String message;

    public InvitationRequest() {}

    // Getters and Setters
    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public Long getCarpoolRequestId() {
        return carpoolRequestId;
    }

    public void setCarpoolRequestId(Long carpoolRequestId) {
        this.carpoolRequestId = carpoolRequestId;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
