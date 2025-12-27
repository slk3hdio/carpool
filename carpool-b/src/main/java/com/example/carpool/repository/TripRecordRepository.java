package com.example.carpool.repository;

import com.example.carpool.entity.TripRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRecordRepository extends JpaRepository<TripRecord, Long> {

    // 根据拼车需求ID查找行程（通过match_record表）
    @Query("SELECT tr FROM TripRecord tr INNER JOIN MatchRecord mr ON tr.id = mr.tripId WHERE mr.requestId = :requestId")
    Optional<TripRecord> findByRequestId(@Param("requestId") Long requestId);

    // 根据状态查找行程
    List<TripRecord> findByStatusDesc(String statusDesc);

    // 查找某个拼车需求是否已有行程
    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TripRecord tr " +
           "INNER JOIN MatchRecord mr ON tr.id = mr.tripId " +
           "WHERE mr.requestId = :requestId")
    boolean existsByRequestId(@Param("requestId") Long requestId);
}
