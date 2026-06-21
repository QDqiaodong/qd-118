package com.weakcurrent.repository;

import com.weakcurrent.entity.WorkshopUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopUsageRepository extends JpaRepository<WorkshopUsage, Long> {

    Optional<WorkshopUsage> findByCode(String code);

    List<WorkshopUsage> findByEnabledTrue();

    @Query("SELECT w FROM WorkshopUsage w ORDER BY w.sort ASC, w.id ASC")
    List<WorkshopUsage> findAllOrderBySort();
}
