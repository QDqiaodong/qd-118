package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.CompatibleModelNormalizer;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.CompatibleModelGroupCreateDTO;
import com.weakcurrent.dto.CompatibleModelGroupUpdateDTO;
import com.weakcurrent.dto.CompatibleModelGroupValidateDTO;
import com.weakcurrent.dto.CompatibleModelGroupValidateResultDTO;
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
    public CompatibleModelGroupValidateResultDTO validate(CompatibleModelGroupValidateDTO dto) {
        String normalizedGroupName = CompatibleModelNormalizer.normalizeGroupName(dto.getGroupName());
        String normalizedBrand = CompatibleModelNormalizer.normalizeBrand(dto.getBrand());
        String normalizedModel = CompatibleModelNormalizer.normalizeModel(dto.getModel());

        if (normalizedGroupName == null || normalizedGroupName.isEmpty()) {
            return CompatibleModelGroupValidateResultDTO.invalid("兼容组名称不能为空");
        }
        if (normalizedModel == null || normalizedModel.isEmpty()) {
            return CompatibleModelGroupValidateResultDTO.invalid("型号不能为空");
        }

        if (isDuplicate(normalizedGroupName, normalizedBrand, normalizedModel, dto.getId())) {
            return CompatibleModelGroupValidateResultDTO.duplicate(
                    ResultCode.COMPATIBLE_MODEL_DUPLICATE.getMessage()
            );
        }

        return CompatibleModelGroupValidateResultDTO.success(
                normalizedGroupName, normalizedBrand, normalizedModel
        );
    }

    @Override
    @Transactional
    public CompatibleModelGroup create(CompatibleModelGroupCreateDTO dto) {
        String groupName = CompatibleModelNormalizer.normalizeGroupName(dto.getGroupName());
        String brand = CompatibleModelNormalizer.normalizeBrand(dto.getBrand());
        String model = CompatibleModelNormalizer.normalizeModel(dto.getModel());
        String spec = CompatibleModelNormalizer.normalizeSpec(dto.getSpec());
        String remark = CompatibleModelNormalizer.normalizeRemark(dto.getRemark());

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

        String groupName = CompatibleModelNormalizer.normalizeGroupName(dto.getGroupName());
        String brand = CompatibleModelNormalizer.normalizeBrand(dto.getBrand());
        String model = CompatibleModelNormalizer.normalizeModel(dto.getModel());
        String spec = CompatibleModelNormalizer.normalizeSpec(dto.getSpec());
        String remark = CompatibleModelNormalizer.normalizeRemark(dto.getRemark());

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
        if (isDuplicate(groupName, brand, model, excludeId)) {
            throw new BusinessException(ResultCode.COMPATIBLE_MODEL_DUPLICATE);
        }
    }

    private boolean isDuplicate(String groupName, String brand, String model, Long excludeId) {
        if (groupName == null || model == null) {
            return false;
        }
        List<CompatibleModelGroup> sameGroup = compatibleModelGroupRepository.findByGroupName(groupName);
        return sameGroup.stream()
                .filter(item -> excludeId == null || !item.getId().equals(excludeId))
                .anyMatch(item -> CompatibleModelNormalizer.brandsEqual(item.getBrand(), brand)
                        && CompatibleModelNormalizer.modelsEqual(item.getModel(), model));
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
