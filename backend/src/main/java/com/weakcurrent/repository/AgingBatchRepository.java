package com.weakcurrent.repository;

import com.weakcurrent.entity.AgingBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgingBatchRepository extends JpaRepository<AgingBatch, Long> {

    List<AgingBatch> findByOperator(String operator);

    List<AgingBatch> findByCategoryId(Long categoryId);

    List<AgingBatch> findByWarehouseZone(String warehouseZone);
}
