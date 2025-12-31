package com.example.carpool.service;

import com.example.carpool.dto.TripResponse;
import com.example.carpool.dto.TripStatusUpdateRequest;
import com.example.carpool.entity.CarpoolInvitation;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.entity.MatchRecord;
import com.example.carpool.entity.TripRecord;
import com.example.carpool.repository.CarpoolInvitationRepository;
import com.example.carpool.repository.CarpoolRequestRepository;
import com.example.carpool.repository.MatchRecordRepository;
import com.example.carpool.repository.TripRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripService {

    @Autowired
    private TripRecordRepository tripRecordRepository;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    @Autowired
    private CarpoolRequestRepository carpoolRequestRepository;

    @Autowired
    private CarpoolInvitationRepository invitationRepository;

    /**
     * 为拼车需求创建初始行程和匹配记录
     * 当第一个邀请被接受时调用
     */
    @Transactional
    public TripRecord createInitialTripForRequest(Long requestId) {
        // 获取拼车需求
        CarpoolRequest request = carpoolRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("拼车需求不存在"));

        // 创建行程
        TripRecord trip = new TripRecord();
        trip.setStartLocation(request.getStartLocation());
        trip.setStartLatitude(request.getStartLatitude());
        trip.setStartLongitude(request.getStartLongitude());
        trip.setEndLocation(request.getEndLocation());
        trip.setEndLatitude(request.getEndLatitude());
        trip.setEndLongitude(request.getEndLongitude());
        trip.setDepartureAt(request.getEarliestDepartureTime());
        trip.setStatusDesc("已创建");
        trip.setPassengerCount(request.getPassengerCount()); // 初始乘客数（车主自己）

        TripRecord savedTrip = tripRecordRepository.save(trip);

        // 为需求发布者创建匹配记录（user_id是需求发布者自己）
        MatchRecord ownerMatch = new MatchRecord(requestId, request.getUserId(), savedTrip.getId());
        matchRecordRepository.save(ownerMatch);

        return savedTrip;
    }

    /**
     * 接受邀请时的完整流程
     * @param invitationId 被接受的邀请ID
     * @return 创建或更新的行程
     */
    @Transactional
    public TripRecord acceptInvitationAndCreateTrip(Long invitationId) {
        // 获取邀请信息
        CarpoolInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));

        Long requestId = invitation.getCarpoolRequestId();
        Long inviterId = invitation.getInviterId();

        // 检查该需求是否已有匹配记录
        Optional<Long> existingTripId = matchRecordRepository.findTripIdByRequestId(requestId);

        TripRecord trip;

        if (existingTripId.isEmpty()) {
            // 没有匹配记录，创建新行程
            trip = createInitialTripForRequest(requestId);
        } else {
            // 已有匹配记录，使用现有行程
            trip = tripRecordRepository.findById(existingTripId.get())
                    .orElseThrow(() -> new IllegalArgumentException("行程不存在"));
        }

        // 检查邀请者是否已经有匹配记录
        if (matchRecordRepository.findByRequestIdAndUserId(requestId, inviterId).isEmpty()) {
            // 为邀请者创建匹配记录
            MatchRecord inviterMatch = new MatchRecord(requestId, inviterId, trip.getId());
            matchRecordRepository.save(inviterMatch);

            // 更新行程的乘客数（添加邀请者的乘客数）
            Long currentPassengerCount = matchRecordRepository.countByTripId(trip.getId());
            trip.setPassengerCount(currentPassengerCount.intValue() + invitation.getPassengerCount());
            tripRecordRepository.save(trip);
        }

        return trip;
    }

    /**
     * 更新行程状态
     */
    @Transactional
    public TripResponse updateTripStatus(Long tripId, TripStatusUpdateRequest request) {
        TripRecord trip = tripRecordRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("行程不存在"));

        // 验证状态
        String status = request.getStatusDesc();
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("无效的行程状态: " + status);
        }

        trip.setStatusDesc(status);
        TripRecord savedTrip = tripRecordRepository.save(trip);

        return buildTripResponse(savedTrip);
    }

    /**
     * 获取行程详情
     */
    public TripResponse getTripById(Long tripId) {
        TripRecord trip = tripRecordRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("行程不存在"));

        return buildTripResponse(trip);
    }

    /**
     * 根据拼车需求ID获取行程
     */
    public TripResponse getTripByRequestId(Long requestId) {
        Long tripId = matchRecordRepository.findTripIdByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("该拼车需求还没有行程"));

        TripRecord trip = tripRecordRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("行程不存在"));

        return buildTripResponse(trip);
    }

    /**
     * 获取用户的所有行程
     * @param userId 用户ID
     * @return 行程列表
     */
    public List<TripResponse> getTripsByUserId(Long userId) {
        // 通过match_record表查找用户参与的所有行程ID
        List<Long> tripIds = matchRecordRepository.findDistinctTripIdsByUserId(userId);

        if (tripIds.isEmpty()) {
            return List.of();
        }

        // 根据行程ID列表获取行程详情，按创建时间倒序
        List<TripRecord> trips = tripRecordRepository.findByIdInOrderByCreatedAtDesc(tripIds);

        // 构建响应对象
        return trips.stream()
                .map(this::buildTripResponse)
                .collect(Collectors.toList());
    }

    /**
     * 验证用户是否有权限操作指定行程
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return true 如果用户有权限，否则抛出异常
     */
    public boolean validateUserPermissionForTrip(Long tripId, Long userId) {
        // 检查行程是否存在
        TripRecord trip = tripRecordRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("行程不存在"));

        // 检查用户是否参与了该行程
        List<MatchRecord> matchRecords = matchRecordRepository.findByTripId(tripId);
        boolean hasPermission = matchRecords.stream()
                .anyMatch(mr -> mr.getUserId().equals(userId));

        if (!hasPermission) {
            throw new IllegalArgumentException("您没有权限操作此行程");
        }

        return true;
    }

    /**
     * 构建行程响应对象
     */
    private TripResponse buildTripResponse(TripRecord trip) {
        TripResponse response = new TripResponse(trip);

        // 加载关联的拼车需求ID列表
        List<MatchRecord> matchRecords = matchRecordRepository.findByTripId(trip.getId());
        List<Long> requestIds = matchRecords.stream()
                .map(MatchRecord::getRequestId)
                .distinct()
                .collect(Collectors.toList());
        response.setRequestIds(requestIds);

        return response;
    }

    /**
     * 验证状态是否有效
     */
    private boolean isValidStatus(String status) {
        return "已创建".equals(status) ||
               "已出发".equals(status) ||
               "已到达".equals(status) ||
               "已取消".equals(status);
    }
}
