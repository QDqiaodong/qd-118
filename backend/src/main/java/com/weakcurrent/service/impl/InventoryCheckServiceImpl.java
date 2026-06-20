package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.InventoryCheckBatchCreateDTO;
import com.weakcurrent.dto.InventoryCheckCreateDTO;
import com.weakcurrent.dto.InventoryCheckUpdateDTO;
import com.weakcurrent.dto.InventoryCheckWizardDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.InventoryCheck;
import com.weakcurrent.repository.InventoryCheckRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.InventoryCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryCheckServiceImpl implements InventoryCheckService {

    private final InventoryCheckRepository inventoryCheckRepository;
    private final AccessoryService accessoryService;

    @Override
    @Transactional
    public InventoryCheck create(InventoryCheckCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        int systemQuantity = accessory.getStockQuantity();
        int difference = dto.getPhysicalQuantity() - systemQuantity;

        if (difference != 0) {
            if (difference > 0) {
                accessoryService.addStock(dto.getAccessoryId(), difference);
            } else {
                if (systemQuantity < -difference) {
                    throw new BusinessException(ResultCode.STOCK_INSUFFICIENT, "清点亏损数量超过现有库存");
                }
                accessoryService.deductStock(dto.getAccessoryId(), -difference);
            }
        }

        InventoryCheck check = new InventoryCheck();
        check.setAccessoryId(dto.getAccessoryId());
        check.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        check.setPhysicalQuantity(dto.getPhysicalQuantity());
        check.setSystemQuantity(systemQuantity);
        check.setDifference(difference);
        check.setCheckPerson(dto.getCheckPerson());
        check.setCheckTime(dto.getCheckTime() != null ? dto.getCheckTime() : LocalDateTime.now());
        check.setRemark(dto.getRemark());

        return inventoryCheckRepository.save(check);
    }

    @Override
    @Transactional
    public List<InventoryCheck> createBatch(InventoryCheckBatchCreateDTO dto) {
        Map<Long, Accessory> accessoryCache = new HashMap<>();
        LocalDateTime checkTime = dto.getCheckTime() != null ? dto.getCheckTime() : LocalDateTime.now();
        List<InventoryCheck> results = new ArrayList<>();

        for (InventoryCheckBatchCreateDTO.InventoryCheckBatchItem item : dto.getDetails()) {
            if (item.getAccessoryId() == null || item.getActualQuantity() == null) {
                continue;
            }

            Accessory accessory = accessoryCache.computeIfAbsent(item.getAccessoryId(),
                    id -> accessoryService.getById(id));

            int systemQuantity = accessory.getStockQuantity();
            int physicalQuantity = item.getActualQuantity();
            int difference = physicalQuantity - systemQuantity;

            if (difference != 0) {
                if (difference > 0) {
                    accessoryService.addStock(accessory.getId(), difference);
                } else {
                    if (systemQuantity < -difference) {
                        throw new BusinessException(ResultCode.STOCK_INSUFFICIENT,
                                "配件【" + accessory.getName() + "】清点亏损数量超过现有库存");
                    }
                    accessoryService.deductStock(accessory.getId(), -difference);
                }
            }

            InventoryCheck check = new InventoryCheck();
            check.setAccessoryId(accessory.getId());
            check.setAccessoryName(accessory.getName());
            check.setPhysicalQuantity(physicalQuantity);
            check.setSystemQuantity(systemQuantity);
            check.setDifference(difference);
            check.setCheckPerson(dto.getCheckPerson());
            check.setCheckTime(checkTime);
            check.setRemark(dto.getRemark());

            results.add(inventoryCheckRepository.save(check));
        }

        return results;
    }

    @Override
    @Transactional
    public List<InventoryCheck> createWizardBatch(InventoryCheckWizardDTO dto) {
        Map<Long, Accessory> accessoryCache = new HashMap<>();
        LocalDateTime checkTime = dto.getCheckTime() != null ? dto.getCheckTime() : LocalDateTime.now();
        List<InventoryCheck> results = new ArrayList<>();

        for (InventoryCheckWizardDTO.InventoryCheckWizardItem item : dto.getItems()) {
            Accessory accessory = accessoryCache.computeIfAbsent(item.getAccessoryId(),
                    id -> accessoryService.getById(id));

            int systemQuantity = accessory.getStockQuantity();
            int physicalQuantity = item.getPhysicalQuantity();
            int difference = physicalQuantity - systemQuantity;

            if (difference != 0) {
                if (difference > 0) {
                    accessoryService.addStock(accessory.getId(), difference);
                } else {
                    if (systemQuantity < -difference) {
                        throw new BusinessException(ResultCode.STOCK_INSUFFICIENT,
                                "配件【" + accessory.getName() + "】清点亏损数量超过现有库存");
                    }
                    accessoryService.deductStock(accessory.getId(), -difference);
                }
            }

            InventoryCheck check = new InventoryCheck();
            check.setAccessoryId(accessory.getId());
            check.setAccessoryName(item.getAccessoryName() != null && !item.getAccessoryName().isEmpty()
                    ? item.getAccessoryName() : accessory.getName());
            check.setPhysicalQuantity(physicalQuantity);
            check.setSystemQuantity(systemQuantity);
            check.setDifference(difference);
            check.setCheckPerson(dto.getCheckPerson());
            check.setCheckTime(checkTime);

            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append("库区: ").append(dto.getWarehouseZone());
            if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
                remarkBuilder.append(" | ").append(dto.getRemark());
            }
            check.setRemark(remarkBuilder.toString());

            results.add(inventoryCheckRepository.save(check));
        }

        return results;
    }

    @Override
    @Transactional
    public InventoryCheck update(InventoryCheckUpdateDTO dto) {
        InventoryCheck check = inventoryCheckRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        int oldDifference = check.getDifference();
        Long originalAccessoryId = check.getAccessoryId();
        if (oldDifference != 0) {
            if (oldDifference > 0) {
                accessoryService.deductStock(originalAccessoryId, oldDifference);
            } else {
                accessoryService.addStock(originalAccessoryId, -oldDifference);
            }
        }

        int newSystemQuantity = accessory.getStockQuantity();
        int newDifference = dto.getPhysicalQuantity() - newSystemQuantity;

        if (newDifference != 0) {
            if (newDifference > 0) {
                accessoryService.addStock(dto.getAccessoryId(), newDifference);
            } else {
                if (newSystemQuantity < -newDifference) {
                    throw new BusinessException(ResultCode.STOCK_INSUFFICIENT, "清点亏损数量超过现有库存");
                }
                accessoryService.deductStock(dto.getAccessoryId(), -newDifference);
            }
        }

        check.setAccessoryId(dto.getAccessoryId());
        check.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        check.setPhysicalQuantity(dto.getPhysicalQuantity());
        check.setSystemQuantity(newSystemQuantity);
        check.setDifference(newDifference);
        check.setCheckPerson(dto.getCheckPerson());
        check.setCheckTime(dto.getCheckTime());
        check.setRemark(dto.getRemark());

        return inventoryCheckRepository.save(check);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        InventoryCheck check = inventoryCheckRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        int difference = check.getDifference();
        if (difference != 0) {
            if (difference > 0) {
                accessoryService.deductStock(check.getAccessoryId(), difference);
            } else {
                accessoryService.addStock(check.getAccessoryId(), -difference);
            }
        }

        inventoryCheckRepository.deleteById(id);
    }

    @Override
    public InventoryCheck getById(Long id) {
        return inventoryCheckRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<InventoryCheck> list() {
        return inventoryCheckRepository.findAll();
    }

    @Override
    public List<InventoryCheck> listByAccessoryId(Long accessoryId) {
        return inventoryCheckRepository.findByAccessoryId(accessoryId);
    }
}
