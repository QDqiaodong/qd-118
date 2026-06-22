package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.AccessoryCreateDTO;
import com.weakcurrent.dto.AccessoryDuplicateDTO;
import com.weakcurrent.dto.AccessoryRelatedRecordsDTO;
import com.weakcurrent.dto.AccessorySpecTemplateDTO;
import com.weakcurrent.dto.AccessoryUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.repository.AccessoryCategoryRepository;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.InventoryCheckRepository;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.repository.StockOutRepository;
import com.weakcurrent.service.AccessoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessoryServiceImpl implements AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final AccessoryCategoryRepository accessoryCategoryRepository;
    private final StockOutRepository stockOutRepository;
    private final InventoryCheckRepository inventoryCheckRepository;
    private final ScrapRecordRepository scrapRecordRepository;

    @Override
    @Transactional
    public Accessory create(AccessoryCreateDTO dto) {
        checkDuplicate(dto.getCategoryId(), dto.getModel(), null);

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
        accessory.setSquareNumber(dto.getSquareNumber());
        accessory.setPinCount(dto.getPinCount());
        accessory.setWidth(dto.getWidth());
        accessory.setHeight(dto.getHeight());
        accessory.setDiameter(dto.getDiameter());

        return accessoryRepository.save(accessory);
    }

    @Override
    @Transactional
    public Accessory update(AccessoryUpdateDTO dto) {
        checkDuplicate(dto.getCategoryId(), dto.getModel(), dto.getId());

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
        accessory.setSquareNumber(dto.getSquareNumber());
        accessory.setPinCount(dto.getPinCount());
        accessory.setWidth(dto.getWidth());
        accessory.setHeight(dto.getHeight());
        accessory.setDiameter(dto.getDiameter());

        return accessoryRepository.save(accessory);
    }

    private void checkDuplicate(Long categoryId, String model, Long excludeId) {
        if (model == null || model.trim().isEmpty()) {
            return;
        }
        List<Accessory> duplicates;
        if (excludeId != null) {
            duplicates = accessoryRepository.findByCategoryIdAndModelAndIdNot(categoryId, model, excludeId);
        } else {
            duplicates = accessoryRepository.findByCategoryIdAndModel(categoryId, model);
        }
        if (!duplicates.isEmpty()) {
            List<AccessoryDuplicateDTO> duplicateInfo = duplicates.stream()
                    .map(a -> new AccessoryDuplicateDTO(a.getId(), a.getName(), a.getModel(), a.getSpec(), a.getWarehouseZone()))
                    .collect(Collectors.toList());
            throw new BusinessException(ResultCode.ACCESSORY_DUPLICATE_MODEL, duplicateInfo);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!accessoryRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        AccessoryRelatedRecordsDTO relatedRecords = checkRelatedRecords(id);
        if (relatedRecords.getTotalCount() > 0) {
            throw new BusinessException(ResultCode.ACCESSORY_HAS_RECORDS, relatedRecords);
        }

        accessoryRepository.deleteById(id);
    }

    private AccessoryRelatedRecordsDTO checkRelatedRecords(Long accessoryId) {
        int stockOutCount = (int) stockOutRepository.countByAccessoryId(accessoryId);
        int inventoryCheckCount = (int) inventoryCheckRepository.countByAccessoryId(accessoryId);
        int scrapCount = (int) scrapRecordRepository.countByAccessoryId(accessoryId);
        return new AccessoryRelatedRecordsDTO(stockOutCount, inventoryCheckCount, scrapCount);
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

    @Override
    public AccessorySpecTemplateDTO getSpecTemplateByCategoryCode(String categoryCode) {
        AccessoryCategory category = accessoryCategoryRepository.findByCode(categoryCode);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return buildSpecTemplate(category);
    }

    @Override
    public List<AccessorySpecTemplateDTO> getAllSpecTemplates() {
        List<AccessoryCategory> rootCategories = accessoryCategoryRepository.findByParentIdOrderBySortAsc(0L);
        return rootCategories.stream()
                .map(this::buildSpecTemplate)
                .collect(Collectors.toList());
    }

    private AccessorySpecTemplateDTO buildSpecTemplate(AccessoryCategory category) {
        List<AccessorySpecTemplateDTO.SpecField> specFields;
        String code = category.getCode();

        if (code != null && code.startsWith("TERMINAL")) {
            specFields = Arrays.asList(
                    new AccessorySpecTemplateDTO.SpecField("squareNumber", "平方数", "string", "如：2.5mm²"),
                    new AccessorySpecTemplateDTO.SpecField("pinCount", "针脚数", "integer", "如：5")
            );
        } else if (code != null && code.startsWith("DUCT")) {
            specFields = Arrays.asList(
                    new AccessorySpecTemplateDTO.SpecField("width", "宽度", "string", "如：25mm"),
                    new AccessorySpecTemplateDTO.SpecField("height", "高度", "string", "如：15mm")
            );
        } else if (code != null && code.startsWith("CLIP")) {
            specFields = Arrays.asList(
                    new AccessorySpecTemplateDTO.SpecField("diameter", "卡扣直径", "string", "如：Φ8mm")
            );
        } else {
            specFields = Collections.emptyList();
        }

        return new AccessorySpecTemplateDTO(
                category.getId(),
                category.getName(),
                category.getCode(),
                specFields
        );
    }
}
