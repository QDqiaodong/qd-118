package com.weakcurrent.service;

import com.weakcurrent.dto.ConstructionProjectCreateDTO;
import com.weakcurrent.dto.ConstructionProjectUpdateDTO;
import com.weakcurrent.dto.ProjectConsumptionDTO;
import com.weakcurrent.entity.ConstructionProject;

import java.util.List;

public interface ConstructionProjectService {

    ConstructionProject create(ConstructionProjectCreateDTO dto);

    ConstructionProject update(ConstructionProjectUpdateDTO dto);

    void delete(Long id);

    ConstructionProject getById(Long id);

    List<ConstructionProject> list();

    List<ConstructionProject> listActive();

    List<ProjectConsumptionDTO> getProjectConsumptionSummary(Long projectId);

    List<ProjectConsumptionDTO> getAllProjectConsumptionSummary();
}
