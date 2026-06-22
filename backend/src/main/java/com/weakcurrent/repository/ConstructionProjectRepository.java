package com.weakcurrent.repository;

import com.weakcurrent.entity.ConstructionProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionProjectRepository extends JpaRepository<ConstructionProject, Long> {

    List<ConstructionProject> findByStatus(Boolean status);

    boolean existsByProjectNo(String projectNo);
}
