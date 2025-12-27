package com.example.carpool.service;

import com.example.carpool.dto.InvitationRequest;
import com.example.carpool.dto.InvitationResponse;
import com.example.carpool.entity.CarpoolInvitation;
import com.example.carpool.entity.CarpoolRequest;
import com.example.carpool.entity.User;
import com.example.carpool.repository.CarpoolInvitationRepository;
import com.example.carpool.repository.CarpoolRequestRepository;
import com.example.carpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    @Autowired
    private CarpoolInvitationRepository invitationRepository;

    @Autowired
    private CarpoolRequestRepository carpoolRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripService tripService;

    /**
     * 创建拼车邀请
     */
    @Transactional
    public InvitationResponse createInvitation(InvitationRequest request) {
        // 1. 验证发起者是否存在
        User inviter = userRepository.findById(request.getInviterId())
                .orElseThrow(() -> new IllegalArgumentException("发起者不存在"));

        // 2. 验证拼车需求是否存在
        CarpoolRequest carpoolRequest = carpoolRequestRepository.findById(request.getCarpoolRequestId())
                .orElseThrow(() -> new IllegalArgumentException("拼车需求不存在"));

        // 3. 验证不能邀请自己发布的拼车需求
        if (carpoolRequest.getUserId().equals(request.getInviterId())) {
            throw new IllegalArgumentException("不能邀请自己发布的拼车需求");
        }

        // 4. 检查是否已经发送过待处理或已接受的邀请
        if (invitationRepository.existsActiveInvitation(request.getInviterId(), request.getCarpoolRequestId())) {
            throw new IllegalArgumentException("您已经向该拼车需求发送过邀请，请勿重复发送");
        }

        // 5. 如果拼车需求有车，验证乘客数
        if (carpoolRequest.getHasCar()) {
            // 获取已接受邀请的总人数
            Integer acceptedPassengers = invitationRepository.sumAcceptedPassengers(request.getCarpoolRequestId());
            int totalPassengers = acceptedPassengers + carpoolRequest.getPassengerCount() + request.getPassengerCount();

            if (totalPassengers > carpoolRequest.getMaxPassengerCount()) {
                throw new IllegalArgumentException(String.format(
                        "乘客人数超出限制。车主最多可载%d人，当前已有%d人，您想添加%d人",
                        carpoolRequest.getMaxPassengerCount(),
                        acceptedPassengers + carpoolRequest.getPassengerCount(),
                        request.getPassengerCount()
                ));
            }
        }

        // 6. 验证乘客数量
        if (request.getPassengerCount() == null || request.getPassengerCount() < 1) {
            throw new IllegalArgumentException("乘客人数必须至少为1人");
        }

        // 7. 创建邀请
        CarpoolInvitation invitation = new CarpoolInvitation();
        invitation.setInviterId(request.getInviterId());
        invitation.setCarpoolRequestId(request.getCarpoolRequestId());
        invitation.setPassengerCount(request.getPassengerCount());
        invitation.setMessage(request.getMessage());
        invitation.setStatus(1); // 待处理

        CarpoolInvitation savedInvitation = invitationRepository.save(invitation);

        // 8. 构建响应
        InvitationResponse response = new InvitationResponse(savedInvitation);
        response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
        response.setInviterPhone(inviter.getPhoneNumber());

        return response;
    }

    /**
     * 获取拼车需求的所有邀请
     */
    public List<InvitationResponse> getInvitationsByRequestId(Long requestId) {
        List<CarpoolInvitation> invitations = invitationRepository.findByCarpoolRequestId(requestId);

        return invitations.stream()
                .map(invitation -> {
                    InvitationResponse response = new InvitationResponse(invitation);

                    // 加载发起者信息
                    userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
                        response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
                        response.setInviterPhone(inviter.getPhoneNumber());
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户发起的所有邀请
     */
    public List<InvitationResponse> getInvitationsByInviterId(Long inviterId) {
        List<CarpoolInvitation> invitations = invitationRepository.findByInviterId(inviterId);

        return invitations.stream()
                .map(invitation -> {
                    InvitationResponse response = new InvitationResponse(invitation);

                    // 加载发起者信息
                    userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
                        response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
                        response.setInviterPhone(inviter.getPhoneNumber());
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户发布的拼车需求收到的所有邀请
     */
    public List<InvitationResponse> getReceivedInvitationsByUserId(Long userId) {
        // 获取用户发布的所有拼车需求
        List<CarpoolRequest> userRequests = carpoolRequestRepository.findAll().stream()
                .filter(req -> req.getUserId().equals(userId))
                .collect(Collectors.toList());

        // 获取这些需求收到的所有邀请
        return userRequests.stream()
                .flatMap(request -> {
                    List<CarpoolInvitation> invitations = invitationRepository.findByCarpoolRequestId(request.getId());
                    return invitations.stream();
                })
                .map(invitation -> {
                    InvitationResponse response = new InvitationResponse(invitation);

                    // 加载发起者信息
                    userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
                        response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
                        response.setInviterPhone(inviter.getPhoneNumber());
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 接受邀请
     */
    @Transactional
    public InvitationResponse acceptInvitation(Long invitationId) {
        CarpoolInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));

        if (invitation.getStatus() != 1) {
            throw new IllegalArgumentException("该邀请已被处理");
        }

        // 再次验证容量
        CarpoolRequest carpoolRequest = carpoolRequestRepository.findById(invitation.getCarpoolRequestId())
                .orElseThrow(() -> new IllegalArgumentException("拼车需求不存在"));

        if (carpoolRequest.getHasCar()) {
            Integer acceptedPassengers = invitationRepository.sumAcceptedPassengers(invitation.getCarpoolRequestId());
            int totalPassengers = acceptedPassengers + carpoolRequest.getPassengerCount() + invitation.getPassengerCount();

            if (totalPassengers > carpoolRequest.getMaxPassengerCount()) {
                throw new IllegalArgumentException("乘客人数已满，无法接受该邀请");
            }
        }

        invitation.setStatus(2); // 已接受
        CarpoolInvitation savedInvitation = invitationRepository.save(invitation);

        // 创建或更新行程（同时创建匹配记录）
        tripService.acceptInvitationAndCreateTrip(savedInvitation.getId());

        InvitationResponse response = new InvitationResponse(savedInvitation);

        userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
            response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
            response.setInviterPhone(inviter.getPhoneNumber());
        });

        return response;
    }

    /**
     * 拒绝邀请
     */
    @Transactional
    public InvitationResponse rejectInvitation(Long invitationId) {
        CarpoolInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));

        if (invitation.getStatus() != 1) {
            throw new IllegalArgumentException("该邀请已被处理");
        }

        invitation.setStatus(3); // 已拒绝
        CarpoolInvitation savedInvitation = invitationRepository.save(invitation);

        InvitationResponse response = new InvitationResponse(savedInvitation);

        userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
            response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
            response.setInviterPhone(inviter.getPhoneNumber());
        });

        return response;
    }

    /**
     * 取消邀请
     */
    @Transactional
    public InvitationResponse cancelInvitation(Long invitationId) {
        CarpoolInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));

        if (invitation.getStatus() != 1) {
            throw new IllegalArgumentException("只能取消待处理的邀请");
        }

        invitation.setStatus(4); // 已取消
        CarpoolInvitation savedInvitation = invitationRepository.save(invitation);

        InvitationResponse response = new InvitationResponse(savedInvitation);

        userRepository.findById(invitation.getInviterId()).ifPresent(inviter -> {
            response.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
            response.setInviterPhone(inviter.getPhoneNumber());
        });

        return response;
    }
}
