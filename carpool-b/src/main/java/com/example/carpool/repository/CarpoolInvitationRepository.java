package com.example.carpool.repository;

import com.example.carpool.entity.CarpoolInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpoolInvitationRepository extends JpaRepository<CarpoolInvitation, Long> {

    // 根据发起者ID查询邀请
    List<CarpoolInvitation> findByInviterId(Long inviterId);

    // 根据拼车需求ID查询邀请
    List<CarpoolInvitation> findByCarpoolRequestId(Long carpoolRequestId);

    // 根据状态查询邀请
    List<CarpoolInvitation> findByStatus(Integer status);

    // 根据发起者ID和状态查询
    List<CarpoolInvitation> findByInviterIdAndStatus(Long inviterId, Integer status);

    // 根据拼车需求ID和状态查询
    List<CarpoolInvitation> findByCarpoolRequestIdAndStatus(Long carpoolRequestId, Integer status);

    // 查询某个拼车需求的所有待处理邀请
    @Query("SELECT i FROM CarpoolInvitation i WHERE i.carpoolRequestId = :requestId AND i.status = 1")
    List<CarpoolInvitation> findPendingInvitationsByRequestId(@Param("requestId") Long requestId);

    // 检查用户是否已经向某个拼车需求发送过待处理的邀请
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM CarpoolInvitation i " +
           "WHERE i.inviterId = :inviterId AND i.carpoolRequestId = :requestId AND i.status IN (1, 2)")
    boolean existsActiveInvitation(@Param("inviterId") Long inviterId, @Param("requestId") Long requestId);

    // 统计某个拼车需求的已接受邀请总人数
    @Query("SELECT COALESCE(SUM(i.passengerCount), 0) FROM CarpoolInvitation i " +
           "WHERE i.carpoolRequestId = :requestId AND i.status = 2")
    Integer sumAcceptedPassengers(@Param("requestId") Long requestId);
}
