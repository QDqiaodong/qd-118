package com.weakcurrent.service;

import com.weakcurrent.dto.InventoryCheckBatchCreateDTO;
import com.weakcurrent.dto.InventoryCheckCreateDTO;
import com.weakcurrent.dto.InventoryCheckUpdateDTO;
import com.weakcurrent.dto.InventoryCheckWizardDTO;
import com.weakcurrent.entity.InventoryCheck;

import java.util.List;

public interface InventoryCheckService {

    InventoryCheck create(InventoryCheckCreateDTO dto);

    List<InventoryCheck> createBatch(InventoryCheckBatchCreateDTO dto);

    List<InventoryCheck> createWizardBatch(InventoryCheckWizardDTO dto);

    InventoryCheck update(InventoryCheckUpdateDTO dto);

    void delete(Long id);

    InventoryCheck getById(Long id);

    List<InventoryCheck> list();

    List<InventoryCheck> listByAccessoryId(Long accessoryId);
}
