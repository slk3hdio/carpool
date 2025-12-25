package com.example.carpool.repository;

import com.example.carpool.entity.CarpoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CarpoolRequestRepository extends JpaRepository<CarpoolRequest, Long> {

    @Query("SELECT r FROM CarpoolRequest r WHERE " +
           "(:statusDesc IS NULL OR r.statusDesc = :statusDesc) AND " +
           "(:startLat IS NULL OR :startLng IS NULL OR " +
           "(6371 * acos(cos(radians(:startLat)) * cos(radians(r.startLatitude)) * " +
           "cos(radians(r.startLongitude) - radians(:startLng)) + " +
           "sin(radians(:startLat)) * sin(radians(r.startLatitude))) <= :radius)) AND " +
           "(:earliestTime IS NULL OR r.earliestDepartureTime >= :earliestTime) AND " +
           "(:latestTime IS NULL OR r.latestDepartureTime <= :latestTime)")
    List<CarpoolRequest> searchRequests(
            @Param("statusDesc") String statusDesc,
            @Param("startLat") Double startLat,
            @Param("startLng") Double startLng,
            @Param("radius") Double radius,
            @Param("earliestTime") LocalDateTime earliestTime,
            @Param("latestTime") LocalDateTime latestTime
    );
}
