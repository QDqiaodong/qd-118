package com.weakcurrent.repository;

import com.weakcurrent.entity.Accessory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Accessory a WHERE a.id = :id")
    Optional<Accessory> findByIdForUpdate(@Param("id") Long id);

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

    @Query("SELECT a FROM Accessory a WHERE a.stockQuantity > 0 " +
           "AND (:categoryIds IS NULL OR a.categoryId IN :categoryIds) " +
           "AND (:warehouseZone IS NULL OR :warehouseZone = '' OR a.warehouseZone = :warehouseZone) " +
           "AND (:inboundStart IS NULL OR a.createTime >= :inboundStart) " +
           "AND (:inboundEnd IS NULL OR a.createTime <= :inboundEnd) " +
           "ORDER BY a.categoryId, a.warehouseZone, a.createTime")
    List<Accessory> findAgingCandidates(@Param("categoryIds") Collection<Long> categoryIds,
                                        @Param("warehouseZone") String warehouseZone,
                                        @Param("inboundStart") LocalDateTime inboundStart,
                                        @Param("inboundEnd") LocalDateTime inboundEnd);
}
