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

    /**
     * 获取用户发布的拼车需求
     * @param userId 用户ID
     * @return 拼车需求列表
     */
    public List<CarpoolRequestResponse> getRequestsByUserId(Long userId) {
        List<CarpoolRequest> requests = carpoolRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return requests.stream()
                .map(request -> {
                    CarpoolRequestResponse response = new CarpoolRequestResponse(request);

                    // 加载用户信息
                    userRepository.findById(request.getUserId()).ifPresent(user -> {
                        response.setUsername(user.getUsername());
                        response.setRealName(user.getRealName());
                        response.setPhoneNumber(user.getPhoneNumber());
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新拼车需求
     * @param requestId 需求ID
     * @param userId 当前用户ID（用于权限验证）
     * @param dto 更新的数据
     * @return 更新后的需求
     */
    @Transactional
    public CarpoolRequest updateRequest(Long requestId, Long userId, CarpoolRequestDto dto) {
        // 查找需求
        CarpoolRequest request = carpoolRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("拼车需求不存在"));

        // 验证权限：只有需求发布者可以修改
        if (!request.getUserId().equals(userId)) {
            throw new IllegalArgumentException("您没有权限修改此拼车需求");
        }

        // 检查需求状态，已匹配或已完成的需求不能修改
        String status = request.getStatusDesc();
        if (status != null && !status.equals("等待匹配") && !status.equals("寻找拼车")) {
            throw new IllegalArgumentException("当前状态不能修改拼车需求");
        }

        // 更新字段
        if (dto.getHasCar() != null) {
            request.setHasCar(dto.getHasCar());
        }
        if (dto.getMaxPassengerCount() != null) {
            request.setMaxPassengerCount(dto.getMaxPassengerCount());
        }
        if (dto.getPassengerCount() != null) {
            request.setPassengerCount(dto.getPassengerCount());
        }
        if (dto.getStartLocation() != null) {
            request.setStartLocation(dto.getStartLocation());
        }
        if (dto.getStartLatitude() != null) {
            request.setStartLatitude(dto.getStartLatitude());
        }
        if (dto.getStartLongitude() != null) {
            request.setStartLongitude(dto.getStartLongitude());
        }
        if (dto.getEndLocation() != null) {
            request.setEndLocation(dto.getEndLocation());
        }
        if (dto.getEndLatitude() != null) {
            request.setEndLatitude(dto.getEndLatitude());
        }
        if (dto.getEndLongitude() != null) {
            request.setEndLongitude(dto.getEndLongitude());
        }
        if (dto.getEarliestDepartureTime() != null) {
            request.setEarliestDepartureTime(dto.getEarliestDepartureTime());
        }
        if (dto.getLatestDepartureTime() != null) {
            request.setLatestDepartureTime(dto.getLatestDepartureTime());
        }
        if (dto.getPhoneNumber() != null) {
            request.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getStatusDesc() != null) {
            request.setStatusDesc(dto.getStatusDesc());
        }

        return carpoolRequestRepository.save(request);
    }
}
