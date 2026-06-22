package com.weakcurrent.service;

import com.weakcurrent.dto.DuctOffcutCreateDTO;
import com.weakcurrent.dto.DuctOffcutReturnDTO;
import com.weakcurrent.entity.DuctOffcut;

import java.util.List;

public interface DuctOffcutService {

    DuctOffcut create(DuctOffcutCreateDTO dto);

    DuctOffcut returnToStock(DuctOffcutReturnDTO dto);

    void delete(Long id);

    DuctOffcut getById(Long id);

    List<DuctOffcut> list();

    List<DuctOffcut> listByAccessoryId(Long accessoryId);

    List<DuctOffcut> listByStatus(Integer status);
}
