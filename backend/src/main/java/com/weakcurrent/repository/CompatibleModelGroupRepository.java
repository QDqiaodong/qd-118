package com.weakcurrent.repository;

import com.weakcurrent.entity.CompatibleModelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompatibleModelGroupRepository extends JpaRepository<CompatibleModelGroup, Long> {

    List<CompatibleModelGroup> findByGroupName(String groupName);

    List<CompatibleModelGroup> findByModel(String model);

    List<CompatibleModelGroup> findByModelContaining(String keyword);

    List<CompatibleModelGroup> findByBrand(String brand);

    List<CompatibleModelGroup> findByAccessoryId(Long accessoryId);

    List<CompatibleModelGroup> findByGroupNameOrderByBrandAsc(String groupName);
}
