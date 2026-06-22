package com.weakcurrent.repository;

import com.weakcurrent.entity.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {

    List<InventoryCheck> findByAccessoryId(Long accessoryId);

    List<InventoryCheck> findByCheckPerson(String checkPerson);

    long countByAccessoryId(Long accessoryId);

    Optional<InventoryCheck> findTopByAccessoryIdOrderByCheckTimeDesc(Long accessoryId);
}
