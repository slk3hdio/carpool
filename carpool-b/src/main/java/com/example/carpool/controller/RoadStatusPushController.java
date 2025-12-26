package com.example.carpool.controller;

import com.example.carpool.websocket.RoadStatusWebSocket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push/road-status")
public class RoadStatusPushController {
    @PostMapping
    public ResponseEntity<String> pushRoadStatus(@RequestBody String message) {
        // 直接广播给所有WebSocket客户端
        RoadStatusWebSocket.broadcast(message);
        return ResponseEntity.ok("Pushed to WebSocket");
    }
}
