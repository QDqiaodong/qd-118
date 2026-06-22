package com.weakcurrent.repository;

import com.weakcurrent.entity.AgingBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgingBatchItemRepository extends JpaRepository<AgingBatchItem, Long> {

    List<AgingBatchItem> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);
}
