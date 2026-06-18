package com.weakcurrent.repository;

import com.weakcurrent.entity.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    List<Accessory> findByCategoryId(Long categoryId);

    List<Accessory> findByNameContaining(String name);

    @Query("SELECT COALESCE(SUM(a.stockQuantity), 0) FROM Accessory a")
    Integer sumAllStockQuantity();
}
