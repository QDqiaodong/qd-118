package com.weakcurrent.repository;

import com.weakcurrent.entity.StockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockOutRepository extends JpaRepository<StockOut, Long> {

    List<StockOut> findByAccessoryId(Long accessoryId);

    List<StockOut> findByOperator(String operator);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockOut s WHERE s.outTime BETWEEN :start AND :end")
    Integer sumQuantityByOutTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
