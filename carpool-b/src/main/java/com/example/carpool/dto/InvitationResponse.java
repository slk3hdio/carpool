package com.example.carpool.dto;

import com.example.carpool.entity.CarpoolInvitation;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class InvitationResponse {
    private Long id;
    private Long inviterId;
    private String inviterName;
    private String inviterPhone;
    private Long carpoolRequestId;
    private Integer passengerCount;
    private String message;
    private Integer status;
    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public InvitationResponse() {}

    public InvitationResponse(CarpoolInvitation invitation) {
        this.id = invitation.getId();
        this.inviterId = invitation.getInviterId();
        this.carpoolRequestId = invitation.getCarpoolRequestId();
        this.passengerCount = invitation.getPassengerCount();
        this.message = invitation.getMessage();
        this.status = invitation.getStatus();
        this.statusDesc = getStatusDescription(invitation.getStatus());
        this.createdAt = invitation.getCreatedAt();
        this.updatedAt = invitation.getUpdatedAt();
    }

    private String getStatusDescription(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待处理";
            case 2: return "已接受";
            case 3: return "已拒绝";
            case 4: return "已取消";
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

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public String getInviterPhone() {
        return inviterPhone;
    }

    public void setInviterPhone(String inviterPhone) {
        this.inviterPhone = inviterPhone;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
