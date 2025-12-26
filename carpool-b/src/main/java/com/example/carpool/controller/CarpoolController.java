package com.example.carpool.controller;

import com.example.carpool.dto.CarpoolRequestDto;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.service.CarpoolService;
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
    public ResponseEntity<List<CarpoolRequest>> searchRequests(
            @RequestParam(required = false) String statusDesc,
            @RequestParam(required = false) Double startLat,
            @RequestParam(required = false) Double startLng,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime earliestTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime latestTime) {
        try {
            List<CarpoolRequest> requests = carpoolService.searchRequests(statusDesc, startLat, startLng, radius, earliestTime, latestTime);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
