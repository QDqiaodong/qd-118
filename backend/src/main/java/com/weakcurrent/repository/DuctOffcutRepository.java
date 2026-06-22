package com.weakcurrent.repository;

import com.weakcurrent.entity.DuctOffcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuctOffcutRepository extends JpaRepository<DuctOffcut, Long> {

    List<DuctOffcut> findByAccessoryId(Long accessoryId);

    List<DuctOffcut> findByStockOutId(Long stockOutId);

    List<DuctOffcut> findByStatus(Integer status);

    List<DuctOffcut> findByAccessoryIdAndStatus(Long accessoryId, Integer status);
}
