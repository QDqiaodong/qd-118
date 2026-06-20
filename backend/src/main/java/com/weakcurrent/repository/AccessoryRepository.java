package com.weakcurrent.repository;

import com.weakcurrent.entity.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    List<Accessory> findByCategoryId(Long categoryId);

    List<Accessory> findByCategoryIdIn(Collection<Long> categoryIds);

    List<Accessory> findByNameContaining(String name);

    List<Accessory> findByWarehouseZone(String warehouseZone);

    List<Accessory> findByWarehouseZoneStartingWith(String zonePrefix);

    @Query("SELECT COALESCE(SUM(a.stockQuantity), 0) FROM Accessory a")
    Integer sumAllStockQuantity();

    @Query("SELECT a.warehouseZone, COUNT(a), COALESCE(SUM(a.stockQuantity), 0) " +
           "FROM Accessory a " +
           "WHERE a.warehouseZone IS NOT NULL AND a.warehouseZone <> '' " +
           "GROUP BY a.warehouseZone")
    List<Object[]> countAndSumByWarehouseZone();
}
