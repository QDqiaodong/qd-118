package com.weakcurrent.repository;

import com.weakcurrent.entity.AccessoryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessoryCategoryRepository extends JpaRepository<AccessoryCategory, Long> {

    List<AccessoryCategory> findByParentIdOrderBySortAsc(Long parentId);

    List<AccessoryCategory> findByParentId(Long parentId);

    List<AccessoryCategory> findAllByOrderBySortAsc();
}
