package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.DuctOffcutCreateDTO;
import com.weakcurrent.dto.DuctOffcutReturnDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.DuctOffcut;
import com.weakcurrent.repository.DuctOffcutRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.DuctOffcutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DuctOffcutServiceImpl implements DuctOffcutService {

    private final DuctOffcutRepository ductOffcutRepository;
    private final AccessoryService accessoryService;

    @Override
    @Transactional
    public DuctOffcut create(DuctOffcutCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        DuctOffcut entity = new DuctOffcut();
        entity.setAccessoryId(dto.getAccessoryId());
        entity.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        entity.setAccessoryModel(dto.getAccessoryModel() != null && !dto.getAccessoryModel().isEmpty()
                ? dto.getAccessoryModel() : accessory.getModel());
        entity.setStockOutId(dto.getStockOutId());
        entity.setOriginalLength(dto.getOriginalLength());
        entity.setUsedLength(dto.getUsedLength());
        entity.setOffcutLength(dto.getOffcutLength());
        entity.setReturnZone(dto.getReturnZone());
        entity.setOperator(dto.getOperator());
        entity.setStatus(0);
        entity.setRemark(dto.getRemark());

        return ductOffcutRepository.save(entity);
    }

    @Override
    @Transactional
    public DuctOffcut returnToStock(DuctOffcutReturnDTO dto) {
        DuctOffcut offcut = ductOffcutRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        if (offcut.getStatus() == 1) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "该余料已回库，不可重复操作");
        }

        int addQuantity = offcut.getOffcutLength().intValue();
        if (addQuantity > 0) {
            accessoryService.addStock(offcut.getAccessoryId(), addQuantity);
        }

        offcut.setStatus(1);
        if (dto.getReturnZone() != null && !dto.getReturnZone().isEmpty()) {
            offcut.setReturnZone(dto.getReturnZone());
        }
        if (dto.getOperator() != null && !dto.getOperator().isEmpty()) {
            offcut.setOperator(dto.getOperator());
        }
        if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
            offcut.setRemark(dto.getRemark());
        }

        return ductOffcutRepository.save(offcut);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!ductOffcutRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        ductOffcutRepository.deleteById(id);
    }

    @Override
    public DuctOffcut getById(Long id) {
        return ductOffcutRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<DuctOffcut> list() {
        return ductOffcutRepository.findAll();
    }

    @Override
    public List<DuctOffcut> listByAccessoryId(Long accessoryId) {
        return ductOffcutRepository.findByAccessoryId(accessoryId);
    }

    @Override
    public List<DuctOffcut> listByStatus(Integer status) {
        return ductOffcutRepository.findByStatus(status);
    }
}
