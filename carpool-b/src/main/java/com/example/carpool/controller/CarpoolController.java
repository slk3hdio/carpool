package com.example.carpool.controller;

import com.example.carpool.dto.CarpoolRequestDto;
import com.example.carpool.dto.CarpoolRequestResponse;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.service.CarpoolService;
import com.example.carpool.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/carpool")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:8080"})
public class CarpoolController {

    @Autowired
    private CarpoolService carpoolService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/request")
    public ResponseEntity<CarpoolRequest> createCarpoolRequest(@RequestBody CarpoolRequestDto dto) {
        try {
            CarpoolRequest createdRequest = carpoolService.createCarpoolRequest(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<CarpoolRequestResponse>> searchRequests(
            @RequestParam(required = false) String statusDesc,
            @RequestParam(required = false) Double startLat,
            @RequestParam(required = false) Double startLng,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime earliestTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime latestTime) {
        try {
            List<CarpoolRequestResponse> requests = carpoolService.searchRequestsWithUserInfo(statusDesc, startLat, startLng, radius, earliestTime, latestTime);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取当前用户发布的拼车需求
     * GET /api/carpool/my-requests
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证token
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("未提供认证令牌"));
            }

            String jwt = token.substring(7);

            // 验证token有效性
            if (!jwtUtil.validateToken(jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("无效的认证令牌"));
            }

            // 从token中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            // 获取用户发布的拼车需求
            List<CarpoolRequestResponse> requests = carpoolService.getRequestsByUserId(userId);

            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取拼车需求失败：" + e.getMessage()));
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
