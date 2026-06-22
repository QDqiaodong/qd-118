package com.weakcurrent.service;

import com.weakcurrent.dto.CompatibleModelGroupCreateDTO;
import com.weakcurrent.dto.CompatibleModelGroupUpdateDTO;
import com.weakcurrent.entity.CompatibleModelGroup;

import java.util.List;

public interface CompatibleModelGroupService {

    CompatibleModelGroup create(CompatibleModelGroupCreateDTO dto);

    CompatibleModelGroup update(CompatibleModelGroupUpdateDTO dto);

    void delete(Long id);

    CompatibleModelGroup getById(Long id);

    List<CompatibleModelGroup> list();

    List<CompatibleModelGroup> listByGroupName(String groupName);

    List<CompatibleModelGroup> listByModel(String model);

    List<CompatibleModelGroup> listByAccessoryId(Long accessoryId);

    List<String> listGroupNames();
}
