package com.example.carpool.repository;

import com.example.carpool.entity.CongestionSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongestionSectionRepository extends JpaRepository<CongestionSection, Long> {

    /**
     * 根据overall_id查询拥堵路段信息
     */
    List<CongestionSection> findByOverallIdOrderByCreatedAtDesc(Long overallId);

    /**
     * 根据道路名称查询拥堵路段信息
     */
    List<CongestionSection> findByRoadNameOrderByCreatedAtDesc(String roadName);

    /**
     * 根据状态查询拥堵路段
     */
    List<CongestionSection> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 根据overall_id和状态查询拥堵路段
     */
    @Query("SELECT cs FROM CongestionSection cs WHERE cs.overallId = :overallId AND cs.status = :status ORDER BY cs.createdAt DESC")
    List<CongestionSection> findByOverallIdAndStatus(@Param("overallId") Long overallId, @Param("status") Integer status);

    /**
     * 获取某条道路的平均速度
     */
    @Query("SELECT AVG(cs.speed) FROM CongestionSection cs WHERE cs.overallId = :overallId AND cs.speed IS NOT NULL")
    Double getAverageSpeedByOverallId(@Param("overallId") Long overallId);

    /**
     * 获取某条道路的总拥堵距离
     */
    @Query("SELECT COALESCE(SUM(cs.congestionDistance), 0) FROM CongestionSection cs WHERE cs.overallId = :overallId AND cs.congestionDistance IS NOT NULL")
    Integer getTotalCongestionDistance(@Param("overallId") Long overallId);
}