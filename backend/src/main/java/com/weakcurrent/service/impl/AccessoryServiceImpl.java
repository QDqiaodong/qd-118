package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.AccessoryCreateDTO;
import com.weakcurrent.dto.AccessoryUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.AccessoryCategoryRepository;
import com.weakcurrent.service.AccessoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessoryServiceImpl implements AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final AccessoryCategoryRepository accessoryCategoryRepository;

    @Override
    @Transactional
    public Accessory create(AccessoryCreateDTO dto) {
        Accessory accessory = new Accessory();
        accessory.setName(dto.getName());
        accessory.setModel(dto.getModel());
        accessory.setSpec(dto.getSpec());
        accessory.setCategoryId(dto.getCategoryId());

        AccessoryCategory category = accessoryCategoryRepository.findById(dto.getCategoryId()).orElse(null);
        if (category != null) {
            accessory.setCategoryName(category.getName());
            accessory.setCategoryPath(buildCategoryPath(dto.getCategoryId()));
        } else if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            accessory.setCategoryName(dto.getCategoryName());
        }

        accessory.setStockQuantity(dto.getStockQuantity());
        accessory.setWarehouseZone(dto.getWarehouseZone());
        accessory.setUnit(dto.getUnit());

        return accessoryRepository.save(accessory);
    }

    @Override
    @Transactional
    public Accessory update(AccessoryUpdateDTO dto) {
        Accessory accessory = accessoryRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessory.setName(dto.getName());
        accessory.setModel(dto.getModel());
        accessory.setSpec(dto.getSpec());
        accessory.setCategoryId(dto.getCategoryId());

        AccessoryCategory category = accessoryCategoryRepository.findById(dto.getCategoryId()).orElse(null);
        if (category != null) {
            accessory.setCategoryName(category.getName());
            accessory.setCategoryPath(buildCategoryPath(dto.getCategoryId()));
        } else if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            accessory.setCategoryName(dto.getCategoryName());
        }

        accessory.setStockQuantity(dto.getStockQuantity());
        accessory.setWarehouseZone(dto.getWarehouseZone());
        accessory.setUnit(dto.getUnit());

        return accessoryRepository.save(accessory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!accessoryRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        accessoryRepository.deleteById(id);
    }

    @Override
    public Accessory getById(Long id) {
        return accessoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<Accessory> list() {
        return accessoryRepository.findAll();
    }

    @Override
    public List<Accessory> listByCategoryId(Long categoryId) {
        return accessoryRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Accessory> searchByName(String name) {
        return accessoryRepository.findByNameContaining(name);
    }

    @Override
    @Transactional
    public void deductStock(Long id, Integer quantity) {
        Accessory accessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        if (accessory.getStockQuantity() < quantity) {
            throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
        }

        accessory.setStockQuantity(accessory.getStockQuantity() - quantity);
        accessoryRepository.save(accessory);
    }

    @Override
    @Transactional
    public void addStock(Long id, Integer quantity) {
        Accessory accessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessory.setStockQuantity(accessory.getStockQuantity() + quantity);
        accessoryRepository.save(accessory);
    }

    @Override
    public String buildCategoryPath(Long categoryId) {
        if (categoryId == null || categoryId.equals(0L)) {
            return "";
        }
        List<AccessoryCategory> allCategories = accessoryCategoryRepository.findAll();
        List<String> pathNames = new ArrayList<>();
        Long currentId = categoryId;
        while (currentId != null && !currentId.equals(0L)) {
            Long lookupId = currentId;
            AccessoryCategory current = allCategories.stream()
                    .filter(c -> c.getId().equals(lookupId))
                    .findFirst()
                    .orElse(null);
            if (current == null) {
                break;
            }
            pathNames.add(current.getName());
            currentId = current.getParentId();
        }
        Collections.reverse(pathNames);
        return String.join("/", pathNames);
    }

    @Override
    @Transactional
    public void syncCategoryPath(Long categoryId, String categoryName, String categoryPath) {
        accessoryRepository.updateCategoryInfoByCategoryId(categoryId, categoryName, categoryPath);
    }

    @Override
    @Transactional
    public void syncCategoryPathForDescendants(List<Long> categoryIds, String categoryName, String categoryPath) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        accessoryRepository.updateCategoryInfoByCategoryIds(categoryIds, categoryName, categoryPath);
    }
}
