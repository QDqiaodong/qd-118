package com.weakcurrent.service;

import com.weakcurrent.dto.WorkshopUsageCreateDTO;
import com.weakcurrent.dto.WorkshopUsageUpdateDTO;
import com.weakcurrent.entity.WorkshopUsage;

import java.util.List;

public interface WorkshopUsageService {

    WorkshopUsage create(WorkshopUsageCreateDTO dto);

    WorkshopUsage update(WorkshopUsageUpdateDTO dto);

    void delete(Long id);

    WorkshopUsage getById(Long id);

    List<WorkshopUsage> list();

    List<WorkshopUsage> listEnabled();

    WorkshopUsage updateStatus(Long id, Boolean enabled);
}
