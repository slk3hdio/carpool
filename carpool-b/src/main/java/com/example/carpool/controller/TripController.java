package com.example.carpool.controller;

import com.example.carpool.dto.TripResponse;
import com.example.carpool.dto.TripStatusUpdateRequest;
import com.example.carpool.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trip")
public class TripController {

    @Autowired
    private TripService tripService;

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
     * 更新行程状态
     * PUT /trip/{tripId}/status
     */
    @PutMapping("/{tripId}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable Long tripId,
            @RequestBody TripStatusUpdateRequest request) {
        try {
            TripResponse trip = tripService.updateTripStatus(tripId, request);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("更新行程状态失败：" + e.getMessage()));
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
