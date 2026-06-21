package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.WorkshopUsageCreateDTO;
import com.weakcurrent.dto.WorkshopUsageUpdateDTO;
import com.weakcurrent.entity.WorkshopUsage;
import com.weakcurrent.repository.WorkshopUsageRepository;
import com.weakcurrent.service.WorkshopUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkshopUsageServiceImpl implements WorkshopUsageService {

    private final WorkshopUsageRepository workshopUsageRepository;

    @Override
    @Transactional
    public WorkshopUsage create(WorkshopUsageCreateDTO dto) {
        if (dto.getCode() != null && !dto.getCode().isEmpty()) {
            Optional<WorkshopUsage> existing = workshopUsageRepository.findByCode(dto.getCode());
            if (existing.isPresent()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "用途编码已存在");
            }
        }

        WorkshopUsage usage = new WorkshopUsage();
        usage.setName(dto.getName());
        usage.setCode(dto.getCode());
        usage.setSort(dto.getSort() != null ? dto.getSort() : 0);
        usage.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        usage.setRemark(dto.getRemark());

        return workshopUsageRepository.save(usage);
    }

    @Override
    @Transactional
    public WorkshopUsage update(WorkshopUsageUpdateDTO dto) {
        WorkshopUsage usage = workshopUsageRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        if (dto.getCode() != null && !dto.getCode().isEmpty()) {
            Optional<WorkshopUsage> existing = workshopUsageRepository.findByCode(dto.getCode());
            if (existing.isPresent() && !existing.get().getId().equals(dto.getId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "用途编码已存在");
            }
        }

        usage.setName(dto.getName());
        usage.setCode(dto.getCode());
        usage.setSort(dto.getSort() != null ? dto.getSort() : 0);
        usage.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        usage.setRemark(dto.getRemark());

        return workshopUsageRepository.save(usage);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WorkshopUsage usage = workshopUsageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
        workshopUsageRepository.delete(usage);
    }

    @Override
    public WorkshopUsage getById(Long id) {
        return workshopUsageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<WorkshopUsage> list() {
        return workshopUsageRepository.findAllOrderBySort();
    }

    @Override
    public List<WorkshopUsage> listEnabled() {
        return workshopUsageRepository.findByEnabledTrue();
    }

    @Override
    @Transactional
    public WorkshopUsage updateStatus(Long id, Boolean enabled) {
        WorkshopUsage usage = workshopUsageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
        usage.setEnabled(enabled);
        return workshopUsageRepository.save(usage);
    }
}
