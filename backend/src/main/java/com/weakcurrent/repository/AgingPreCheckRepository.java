package com.weakcurrent.repository;

import com.weakcurrent.entity.AgingPreCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgingPreCheckRepository extends JpaRepository<AgingPreCheck, Long> {

    List<AgingPreCheck> findByAccessoryId(Long accessoryId);

    List<AgingPreCheck> findByThresholdReached(Boolean thresholdReached);

    List<AgingPreCheck> findByScrapRecordIdIsNullAndThresholdReachedTrue();
}
