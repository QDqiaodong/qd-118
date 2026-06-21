package com.weakcurrent.repository;

import com.weakcurrent.entity.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Accessory> findByCategoryIdAndModelAndIdNot(Long categoryId, String model, Long excludeId);

    List<Accessory> findByCategoryIdAndModel(Long categoryId, String model);

    @Query("SELECT COALESCE(SUM(a.stockQuantity), 0) FROM Accessory a")
    Integer sumAllStockQuantity();

    @Query("SELECT a.warehouseZone, COUNT(a), COALESCE(SUM(a.stockQuantity), 0) " +
           "FROM Accessory a " +
           "WHERE a.warehouseZone IS NOT NULL AND a.warehouseZone <> '' " +
           "GROUP BY a.warehouseZone")
    List<Object[]> countAndSumByWarehouseZone();

    @Modifying
    @Query("UPDATE Accessory a SET a.categoryName = :categoryName, a.categoryPath = :categoryPath WHERE a.categoryId = :categoryId")
    int updateCategoryInfoByCategoryId(@Param("categoryId") Long categoryId,
                                       @Param("categoryName") String categoryName,
                                       @Param("categoryPath") String categoryPath);

    @Modifying
    @Query("UPDATE Accessory a SET a.categoryName = :categoryName, a.categoryPath = :categoryPath WHERE a.categoryId IN :categoryIds")
    int updateCategoryInfoByCategoryIds(@Param("categoryIds") Collection<Long> categoryIds,
                                        @Param("categoryName") String categoryName,
                                        @Param("categoryPath") String categoryPath);
}
