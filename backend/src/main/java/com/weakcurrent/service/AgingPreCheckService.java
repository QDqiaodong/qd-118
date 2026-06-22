package com.weakcurrent.service;

import com.weakcurrent.dto.AgingPreCheckBatchDTO;
import com.weakcurrent.dto.AgingPreCheckCreateDTO;
import com.weakcurrent.dto.AgingPreCheckUpdateDTO;
import com.weakcurrent.entity.AgingPreCheck;

import java.util.List;

public interface AgingPreCheckService {

    AgingPreCheck create(AgingPreCheckCreateDTO dto);

    List<AgingPreCheck> createBatch(AgingPreCheckBatchDTO dto);

    AgingPreCheck update(AgingPreCheckUpdateDTO dto);

    void delete(Long id);

    AgingPreCheck getById(Long id);

    List<AgingPreCheck> list();

    List<AgingPreCheck> listByAccessoryId(Long accessoryId);

    List<AgingPreCheck> listPendingScrap();

    Integer calculateOverallLevel(Integer yellowing, Integer cracking, Integer oxidation);

    boolean checkThresholdReached(Integer overallLevel);
}
