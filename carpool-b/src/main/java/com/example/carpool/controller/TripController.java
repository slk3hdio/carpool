package com.example.carpool.controller;

import com.example.carpool.dto.TripResponse;
import com.example.carpool.dto.TripStatusUpdateRequest;
import com.example.carpool.service.TripService;
import com.example.carpool.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trip")
// @CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:8080"})
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取行程详情
     * GET /trip/{tripId}
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTrip(@PathVariable Long tripId) {
        try {
            TripResponse trip = tripService.getTripById(tripId);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取行程失败：" + e.getMessage()));
        }
    }

    /**
     * 根据拼车需求ID获取行程
     * GET /trip/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<?> getTripByRequestId(@PathVariable Long requestId) {
        try {
            TripResponse trip = tripService.getTripByRequestId(requestId);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取行程失败：" + e.getMessage()));
        }
    }

    /**
     * 获取当前用户的所有行程
     * GET /trip/user
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserTrips(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证token
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("未提供认证令牌"));
            }

            String jwt = token.substring(7); // 移除 "Bearer " 前缀

            // 验证token有效性
            if (!jwtUtil.validateToken(jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("无效的认证令牌"));
            }

            // 从token中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            // 获取用户的所有行程
            List<TripResponse> trips = tripService.getTripsByUserId(userId);

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取用户行程失败：" + e.getMessage()));
        }
    }

    /**
     * 更新行程状态
     * PUT /trip/{tripId}/status
     */
    @PutMapping("/{tripId}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable Long tripId,
            @RequestBody TripStatusUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
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

            // 验证用户是否有权限操作此行程
            tripService.validateUserPermissionForTrip(tripId, userId);

            TripResponse trip = tripService.updateTripStatus(tripId, request);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("更新行程状态失败：" + e.getMessage()));
        }
    }

    /**
     * 取消行程
     * PUT /trip/{tripId}/cancel
     */
    @PutMapping("/{tripId}/cancel")
    public ResponseEntity<?> cancelTrip(
            @PathVariable Long tripId,
            @RequestHeader(value = "Authorization", required = false) String token) {
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

            // 验证用户是否有权限操作此行程
            tripService.validateUserPermissionForTrip(tripId, userId);

            // 创建取消请求
            TripStatusUpdateRequest cancelRequest = new TripStatusUpdateRequest();
            cancelRequest.setStatusDesc("已取消");

            TripResponse trip = tripService.updateTripStatus(tripId, cancelRequest);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("取消行程失败：" + e.getMessage()));
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
