package com.example.carpool.repository;

import com.example.carpool.entity.RoadTrafficOverall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoadTrafficRepository extends JpaRepository<RoadTrafficOverall, Long> {

    /**
     * 根据道路名称和城市查询最新的路况信息
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE r.roadName = :roadName AND r.city = :city ORDER BY r.requestTime DESC")
    Page<RoadTrafficOverall> findLatestByRoadAndCity(@Param("roadName") String roadName, @Param("city") String city, Pageable pageable);

    /**
     * 根据城市查询所有路况信息
     */
    List<RoadTrafficOverall> findByCityOrderByRequestTimeDesc(String city);

    /**
     * 根据城市查询路况信息，分页
     */
    Page<RoadTrafficOverall> findByCityOrderByRequestTimeDesc(String city, Pageable pageable);

    /**
     * 根据拥堵状态查询路况信息
     */
    List<RoadTrafficOverall> findByEvaluationStatusOrderByRequestTimeDesc(Integer evaluationStatus);

    /**
     * 查询指定时间之后的所有路况信息
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE r.requestTime >= :since ORDER BY r.requestTime DESC")
    List<RoadTrafficOverall> findLatestSince(@Param("since") LocalDateTime since);

    /**
     * 获取每个道路的最新路况信息
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE r.id IN " +
           "(SELECT MAX(r2.id) FROM RoadTrafficOverall r2 GROUP BY r2.roadName, r2.city) " +
           "ORDER BY r.requestTime DESC")
    List<RoadTrafficOverall> findLatestForEachRoad();

    /**
     * 获取每个道路的最新路况信息，分页
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE r.id IN " +
           "(SELECT MAX(r2.id) FROM RoadTrafficOverall r2 GROUP BY r2.roadName, r2.city) " +
           "ORDER BY r.requestTime DESC")
    Page<RoadTrafficOverall> findLatestForEachRoad(Pageable pageable);

    /**
     * 根据道路名称模糊查询
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE r.roadName LIKE %:keyword% OR r.city LIKE %:keyword% ORDER BY r.requestTime DESC")
    List<RoadTrafficOverall> findByKeyword(@Param("keyword") String keyword);

    /**
     * 获取路况统计信息
     */
    @Query("SELECT r.evaluationStatus, COUNT(r) FROM RoadTrafficOverall r WHERE r.requestTime >= :since GROUP BY r.evaluationStatus")
    List<Object[]> getTrafficStatsSince(@Param("since") LocalDateTime since);

    /**
     * 检查道路是否存在
     */
    boolean existsByRoadNameAndCity(String roadName, String city);

    // ========== 历史数据查询相关方法 ==========

    /**
     * 获取指定道路在指定时间范围内的历史数据
     */
    @Query("SELECT r FROM RoadTrafficOverall r WHERE " +
           "r.roadName = :roadName AND r.city = :city AND " +
           "r.requestTime BETWEEN :startTime AND :endTime " +
           "ORDER BY r.requestTime DESC")
    Page<RoadTrafficOverall> findHistoricalTraffic(
            @Param("roadName") String roadName,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 获取所有不重复的城市列表
     */
    @Query("SELECT DISTINCT r.city FROM RoadTrafficOverall r ORDER BY r.city")
    List<String> findDistinctCities();

    /**
     * 获取指定城市的所有不重复道路列表
     */
    @Query("SELECT DISTINCT r.roadName FROM RoadTrafficOverall r WHERE r.city = :city ORDER BY r.roadName")
    List<String> findDistinctRoadsByCity(@Param("city") String city);

    /**
     * 获取指定道路最新的平均速度
     */
    @Query("SELECT AVG(cs.speed) FROM CongestionSection cs WHERE cs.overallId IN " +
           "(SELECT r.id FROM RoadTrafficOverall r WHERE r.roadName = :roadName AND r.city = :city " +
           "AND r.requestTime BETWEEN :startTime AND :endTime) AND cs.speed IS NOT NULL")
    Double getAverageSpeedByRoadAndTimeRange(
            @Param("roadName") String roadName,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定道路在指定时间范围内的拥堵统计
     */
    @Query("SELECT r.evaluationStatus, COUNT(r) FROM RoadTrafficOverall r WHERE " +
           "r.roadName = :roadName AND r.city = :city AND " +
           "r.requestTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.evaluationStatus")
    List<Object[]> getCongestionStatsByRoadAndTimeRange(
            @Param("roadName") String roadName,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查时间范围是否超过限制（防止查询过多数据）
     */
    @Query("SELECT COUNT(r) FROM RoadTrafficOverall r WHERE " +
           "r.roadName = :roadName AND r.city = :city AND " +
           "r.requestTime BETWEEN :startTime AND :endTime")
    Long countHistoricalTraffic(
            @Param("roadName") String roadName,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

}