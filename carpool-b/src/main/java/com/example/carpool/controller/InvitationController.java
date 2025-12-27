package com.example.carpool.controller;

import com.example.carpool.dto.InvitationRequest;
import com.example.carpool.dto.InvitationResponse;
import com.example.carpool.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carpool/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    /**
     * 创建拼车邀请
     * POST /api/carpool/invitation
     */
    @PostMapping
    public ResponseEntity<?> createInvitation(@RequestBody InvitationRequest request) {
        try {
            InvitationResponse response = invitationService.createInvitation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("创建邀请失败：" + e.getMessage()));
        }
    }

    /**
     * 获取某个拼车需求的所有邀请
     * GET /api/carpool/invitation/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<?> getInvitationsByRequestId(@PathVariable Long requestId) {
        try {
            List<InvitationResponse> invitations = invitationService.getInvitationsByRequestId(requestId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取邀请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户发起的所有邀请
     * GET /api/carpool/invitation/inviter/{inviterId}
     */
    @GetMapping("/inviter/{inviterId}")
    public ResponseEntity<?> getInvitationsByInviterId(@PathVariable Long inviterId) {
        try {
            List<InvitationResponse> invitations = invitationService.getInvitationsByInviterId(inviterId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取邀请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户收到的所有邀请（别人向用户发布的拼车需求发送的邀请）
     * GET /api/carpool/invitation/received/{userId}
     */
    @GetMapping("/received/{userId}")
    public ResponseEntity<?> getReceivedInvitations(@PathVariable Long userId) {
        try {
            List<InvitationResponse> invitations = invitationService.getReceivedInvitationsByUserId(userId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取收到的邀请失败：" + e.getMessage()));
        }
    }

    /**
     * 接受邀请
     * PUT /api/carpool/invitation/{id}/accept
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long id) {
        try {
            InvitationResponse response = invitationService.acceptInvitation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("接受邀请失败：" + e.getMessage()));
        }
    }

    /**
     * 拒绝邀请
     * PUT /api/carpool/invitation/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectInvitation(@PathVariable Long id) {
        try {
            InvitationResponse response = invitationService.rejectInvitation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("拒绝邀请失败：" + e.getMessage()));
        }
    }

    /**
     * 取消邀请
     * PUT /api/carpool/invitation/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long id) {
        try {
            InvitationResponse response = invitationService.cancelInvitation(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("取消邀请失败：" + e.getMessage()));
        }
    }

    /**
     * 错误响应体
     */
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
