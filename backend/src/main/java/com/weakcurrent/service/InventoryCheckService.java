package com.weakcurrent.service;

import com.weakcurrent.dto.InventoryCheckCreateDTO;
import com.weakcurrent.dto.InventoryCheckUpdateDTO;
import com.weakcurrent.entity.InventoryCheck;

import java.util.List;

public interface InventoryCheckService {

    InventoryCheck create(InventoryCheckCreateDTO dto);

    InventoryCheck update(InventoryCheckUpdateDTO dto);

    void delete(Long id);

    InventoryCheck getById(Long id);

    List<InventoryCheck> list();

    List<InventoryCheck> listByAccessoryId(Long accessoryId);
}
