package com.example.carpool.service;

import com.example.carpool.dto.CarpoolRequestDto;
import com.example.carpool.dto.CarpoolRequestResponse;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.entity.User;
import com.example.carpool.repository.CarpoolRequestRepository;
import com.example.carpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarpoolService {

    @Autowired
    private CarpoolRequestRepository carpoolRequestRepository;

    @Autowired
    private UserRepository userRepository;

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

    /**
     * 获取带用户信息的拼车需求列表
     */
    public List<CarpoolRequestResponse> searchRequestsWithUserInfo(String statusDesc, Double startLat, Double startLng,
                                                                    Double radius, LocalDateTime earliestTime, LocalDateTime latestTime) {
        List<CarpoolRequest> requests = carpoolRequestRepository.searchRequests(statusDesc, startLat, startLng, radius, earliestTime, latestTime);

        return requests.stream()
                .map(request -> {
                    CarpoolRequestResponse response = new CarpoolRequestResponse(request);

                    // 加载用户信息
                    userRepository.findById(request.getUserId()).ifPresent(user -> {
                        response.setUsername(user.getUsername());
                        response.setRealName(user.getRealName());
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }
}
