package com.example.carpool.service;

import com.example.carpool.dto.CarpoolRequestDto;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.repository.CarpoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarpoolService {

    @Autowired
    private CarpoolRequestRepository carpoolRequestRepository;

    @Transactional
    public CarpoolRequest createCarpoolRequest(CarpoolRequestDto dto) {
        CarpoolRequest request = new CarpoolRequest();
        request.setUserId(dto.getUserId());
        request.setHasCar(dto.getHasCar());
        request.setMaxPassengerCount(dto.getMaxPassengerCount());
        request.setPassengerCount(dto.getPassengerCount());
        request.setStartLocation(dto.getStartLocation());
        request.setStartLatitude(dto.getStartLatitude());
        request.setStartLongitude(dto.getStartLongitude());
        request.setEndLocation(dto.getEndLocation());
        request.setEndLatitude(dto.getEndLatitude());
        request.setEndLongitude(dto.getEndLongitude());
        request.setEarliestDepartureTime(dto.getEarliestDepartureTime());
        request.setLatestDepartureTime(dto.getLatestDepartureTime());
        request.setPhoneNumber(dto.getPhoneNumber());
        request.setStatusDesc(dto.getStatusDesc());

        return carpoolRequestRepository.save(request);
    }

    public List<CarpoolRequest> searchRequests(String statusDesc, Double startLat, Double startLng,
                                                Double radius, LocalDateTime earliestTime, LocalDateTime latestTime) {
        return carpoolRequestRepository.searchRequests(statusDesc, startLat, startLng, radius, earliestTime, latestTime);
    }
}
