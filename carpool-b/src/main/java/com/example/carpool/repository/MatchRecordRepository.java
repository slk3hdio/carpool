package com.example.carpool.repository;

import com.example.carpool.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {

    // 根据拼车需求ID查找所有匹配记录
    List<MatchRecord> findByRequestId(Long requestId);

    // 根据用户ID查找所有匹配记录
    List<MatchRecord> findByUserId(Long userId);

    // 根据行程ID查找所有匹配记录
    List<MatchRecord> findByTripId(Long tripId);

    // 根据拼车需求ID和用户ID查找匹配记录
    Optional<MatchRecord> findByRequestIdAndUserId(Long requestId, Long userId);

    // 查找某个拼车需求是否已有行程
    @Query("SELECT mr.tripId FROM MatchRecord mr WHERE mr.requestId = :requestId")
    Optional<Long> findTripIdByRequestId(@Param("requestId") Long requestId);

    // 统计某个行程的乘客数（匹配记录数量）
    @Query("SELECT COUNT(mr) FROM MatchRecord mr WHERE mr.tripId = :tripId")
    Long countByTripId(@Param("tripId") Long tripId);

    // 删除某个拼车需求的所有匹配记录
    void deleteByRequestId(Long requestId);

    // 删除某个行程的所有匹配记录
    void deleteByTripId(Long tripId);
}
