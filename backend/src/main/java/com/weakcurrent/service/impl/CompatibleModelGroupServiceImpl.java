package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.CompatibleModelGroupCreateDTO;
import com.weakcurrent.dto.CompatibleModelGroupUpdateDTO;
import com.weakcurrent.entity.CompatibleModelGroup;
import com.weakcurrent.repository.CompatibleModelGroupRepository;
import com.weakcurrent.service.CompatibleModelGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompatibleModelGroupServiceImpl implements CompatibleModelGroupService {

    private final CompatibleModelGroupRepository compatibleModelGroupRepository;

    @Override
    @Transactional
    public CompatibleModelGroup create(CompatibleModelGroupCreateDTO dto) {
        CompatibleModelGroup entity = new CompatibleModelGroup();
        entity.setGroupName(dto.getGroupName());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setSpec(dto.getSpec());
        entity.setAccessoryId(dto.getAccessoryId());
        entity.setRemark(dto.getRemark());
        return compatibleModelGroupRepository.save(entity);
    }

    @Override
    @Transactional
    public CompatibleModelGroup update(CompatibleModelGroupUpdateDTO dto) {
        CompatibleModelGroup entity = compatibleModelGroupRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
        entity.setGroupName(dto.getGroupName());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setSpec(dto.getSpec());
        entity.setAccessoryId(dto.getAccessoryId());
        entity.setRemark(dto.getRemark());
        return compatibleModelGroupRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!compatibleModelGroupRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        compatibleModelGroupRepository.deleteById(id);
    }

    @Override
    public CompatibleModelGroup getById(Long id) {
        return compatibleModelGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<CompatibleModelGroup> list() {
        return compatibleModelGroupRepository.findAll();
    }

    @Override
    public List<CompatibleModelGroup> listByGroupName(String groupName) {
        return compatibleModelGroupRepository.findByGroupNameOrderByBrandAsc(groupName);
    }

    @Override
    public List<CompatibleModelGroup> listByModel(String model) {
        return compatibleModelGroupRepository.findByModel(model);
    }

    @Override
    public List<CompatibleModelGroup> listByAccessoryId(Long accessoryId) {
        return compatibleModelGroupRepository.findByAccessoryId(accessoryId);
    }

    @Override
    public List<String> listGroupNames() {
        return compatibleModelGroupRepository.findAll().stream()
                .map(CompatibleModelGroup::getGroupName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
