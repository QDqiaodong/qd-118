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
        String groupName = normalize(dto.getGroupName());
        String brand = normalizeNullable(dto.getBrand());
        String model = normalize(dto.getModel());
        String spec = normalizeNullable(dto.getSpec());
        String remark = normalizeNullable(dto.getRemark());

        checkDuplicate(groupName, brand, model, null);

        CompatibleModelGroup entity = new CompatibleModelGroup();
        entity.setGroupName(groupName);
        entity.setBrand(brand);
        entity.setModel(model);
        entity.setSpec(spec);
        entity.setAccessoryId(dto.getAccessoryId());
        entity.setRemark(remark);
        return compatibleModelGroupRepository.save(entity);
    }

    @Override
    @Transactional
    public CompatibleModelGroup update(CompatibleModelGroupUpdateDTO dto) {
        CompatibleModelGroup entity = compatibleModelGroupRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        String groupName = normalize(dto.getGroupName());
        String brand = normalizeNullable(dto.getBrand());
        String model = normalize(dto.getModel());
        String spec = normalizeNullable(dto.getSpec());
        String remark = normalizeNullable(dto.getRemark());

        checkDuplicate(groupName, brand, model, dto.getId());

        entity.setGroupName(groupName);
        entity.setBrand(brand);
        entity.setModel(model);
        entity.setSpec(spec);
        entity.setAccessoryId(dto.getAccessoryId());
        entity.setRemark(remark);
        return compatibleModelGroupRepository.save(entity);
    }

    private void checkDuplicate(String groupName, String brand, String model, Long excludeId) {
        if (groupName == null || model == null) {
            return;
        }
        String normGroup = groupName.trim();
        String normBrand = (brand == null ? "" : brand).trim().toLowerCase();
        String normModel = model.trim().toLowerCase();

        List<CompatibleModelGroup> sameGroup = compatibleModelGroupRepository.findByGroupName(normGroup);
        boolean duplicate = sameGroup.stream()
                .filter(item -> excludeId == null || !item.getId().equals(excludeId))
                .anyMatch(item -> {
                    String itemBrand = (item.getBrand() == null ? "" : item.getBrand()).trim().toLowerCase();
                    String itemModel = (item.getModel() == null ? "" : item.getModel()).trim().toLowerCase();
                    return itemBrand.equals(normBrand) && itemModel.equals(normModel);
                });
        if (duplicate) {
            throw new BusinessException(ResultCode.COMPATIBLE_MODEL_DUPLICATE);
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
