package com.weakcurrent.repository;

import com.weakcurrent.entity.ScrapRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScrapRecordRepository extends JpaRepository<ScrapRecord, Long> {

    List<ScrapRecord> findByAccessoryId(Long accessoryId);

    List<ScrapRecord> findByOperator(String operator);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM ScrapRecord s WHERE s.scrapTime BETWEEN :start AND :end")
    Integer sumQuantityByScrapTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.warehouseZone, COALESCE(SUM(s.quantity), 0) " +
           "FROM ScrapRecord s " +
           "JOIN Accessory a ON s.accessoryId = a.id " +
           "WHERE a.warehouseZone IS NOT NULL AND a.warehouseZone <> '' " +
           "GROUP BY a.warehouseZone")
    List<Object[]> sumScrapQuantityByWarehouseZone();
}
