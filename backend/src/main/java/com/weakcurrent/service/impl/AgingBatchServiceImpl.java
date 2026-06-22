package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.AgingBatchCreateDTO;
import com.weakcurrent.dto.AgingBatchGenerateDTO;
import com.weakcurrent.dto.AgingBatchItemDTO;
import com.weakcurrent.dto.AgingBatchUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.entity.AgingBatch;
import com.weakcurrent.entity.AgingBatchItem;
import com.weakcurrent.entity.ScrapRecord;
import com.weakcurrent.repository.AccessoryCategoryRepository;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.AgingBatchItemRepository;
import com.weakcurrent.repository.AgingBatchRepository;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.AgingBatchService;
import com.weakcurrent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgingBatchServiceImpl implements AgingBatchService {

    private final AgingBatchRepository agingBatchRepository;
    private final AgingBatchItemRepository agingBatchItemRepository;
    private final AccessoryRepository accessoryRepository;
    private final AccessoryCategoryRepository accessoryCategoryRepository;
    private final AccessoryService accessoryService;
    private final ScrapRecordRepository scrapRecordRepository;
    private final DashboardService dashboardService;

    private static final DateTimeFormatter BATCH_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public List<AgingBatchItemDTO> generatePreview(AgingBatchGenerateDTO dto) {
        List<Long> categoryIds = null;
        if (dto.getCategoryId() != null) {
            categoryIds = collectAllCategoryIds(dto.getCategoryId());
        }

        List<Accessory> accessories = accessoryRepository.findAgingCandidates(
                categoryIds,
                dto.getWarehouseZone(),
                dto.getInboundStart(),
                dto.getInboundEnd()
        );

        return accessories.stream().map(a -> {
            AgingBatchItemDTO item = new AgingBatchItemDTO();
            item.setAccessoryId(a.getId());
            item.setAccessoryName(a.getName());
            item.setAccessoryModel(a.getModel());
            item.setUnit(a.getUnit());
            item.setWarehouseZone(a.getWarehouseZone());
            item.setCategoryId(a.getCategoryId());
            item.setCategoryName(a.getCategoryName());
            item.setStockQuantity(a.getStockQuantity());
            item.setScrapQuantity(a.getStockQuantity());
            item.setInboundTime(a.getCreateTime());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Long> collectAllCategoryIds(Long parentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(parentId);
        List<AccessoryCategory> children = accessoryCategoryRepository.findByParentId(parentId);
        for (AccessoryCategory child : children) {
            ids.addAll(collectAllCategoryIds(child.getId()));
        }
        return ids;
    }

    @Override
    @Transactional
    public AgingBatch create(AgingBatchCreateDTO dto) {
        Map<Long, Integer> mergedQuantities = new HashMap<>();
        for (AgingBatchItemDTO item : dto.getItems()) {
            mergedQuantities.merge(item.getAccessoryId(), item.getScrapQuantity(), Integer::sum);
        }

        Map<Long, Accessory> accessoryCache = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : mergedQuantities.entrySet()) {
            Long accessoryId = entry.getKey();
            Integer totalQuantity = entry.getValue();

            Accessory accessory = accessoryService.getById(accessoryId);
            accessoryCache.put(accessoryId, accessory);

            if (accessory.getStockQuantity() < totalQuantity) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("配件【%s - %s】合并报废数量 %d 超过库存 %d",
                                accessory.getName(), accessory.getModel(), totalQuantity, accessory.getStockQuantity()));
            }
        }

        for (Map.Entry<Long, Integer> entry : mergedQuantities.entrySet()) {
            accessoryService.deductStock(entry.getKey(), entry.getValue());
        }

        AgingBatch batch = new AgingBatch();
        batch.setBatchNo("AG" + LocalDateTime.now().format(BATCH_NO_FORMATTER));
        batch.setCategoryId(dto.getCategoryId());
        batch.setCategoryName(dto.getCategoryName());
        batch.setWarehouseZone(dto.getWarehouseZone());
        batch.setInboundStart(dto.getInboundStart());
        batch.setInboundEnd(dto.getInboundEnd());
        batch.setReason(dto.getReason());
        batch.setOperator(dto.getOperator());
        batch.setArchiveTime(LocalDateTime.now());
        batch.setRemark(dto.getRemark());

        AtomicInteger totalQty = new AtomicInteger(0);
        dto.getItems().forEach(item -> totalQty.addAndGet(item.getScrapQuantity()));
        batch.setTotalQuantity(totalQty.get());
        batch.setItemCount(dto.getItems().size());

        AgingBatch savedBatch = agingBatchRepository.save(batch);

        LocalDateTime now = LocalDateTime.now();
        for (AgingBatchItemDTO itemDTO : dto.getItems()) {
            Accessory accessory = accessoryCache.get(itemDTO.getAccessoryId());

            AgingBatchItem item = new AgingBatchItem();
            item.setBatchId(savedBatch.getId());
            item.setAccessoryId(itemDTO.getAccessoryId());
            item.setAccessoryName(itemDTO.getAccessoryName() != null && !itemDTO.getAccessoryName().isEmpty()
                    ? itemDTO.getAccessoryName() : accessory.getName());
            item.setAccessoryModel(itemDTO.getAccessoryModel() != null && !itemDTO.getAccessoryModel().isEmpty()
                    ? itemDTO.getAccessoryModel() : accessory.getModel());
            item.setUnit(accessory.getUnit());
            item.setWarehouseZone(itemDTO.getWarehouseZone() != null ? itemDTO.getWarehouseZone() : accessory.getWarehouseZone());
            item.setCategoryId(itemDTO.getCategoryId() != null ? itemDTO.getCategoryId() : accessory.getCategoryId());
            item.setCategoryName(itemDTO.getCategoryName() != null ? itemDTO.getCategoryName() : accessory.getCategoryName());
            item.setStockQuantity(itemDTO.getStockQuantity() != null ? itemDTO.getStockQuantity() : accessory.getStockQuantity() + itemDTO.getScrapQuantity());
            item.setScrapQuantity(itemDTO.getScrapQuantity());
            item.setInboundTime(itemDTO.getInboundTime() != null ? itemDTO.getInboundTime() : accessory.getCreateTime());
            item.setReason(itemDTO.getReason() != null && !itemDTO.getReason().isEmpty()
                    ? itemDTO.getReason() : dto.getReason());
            item.setRemark(itemDTO.getRemark());
            agingBatchItemRepository.save(item);

            ScrapRecord scrap = new ScrapRecord();
            scrap.setAccessoryId(itemDTO.getAccessoryId());
            scrap.setAccessoryName(item.getAccessoryName());
            scrap.setAccessoryModel(item.getAccessoryModel());
            scrap.setUnit(accessory.getUnit());
            scrap.setQuantity(itemDTO.getScrapQuantity());
            scrap.setReason(item.getReason());
            scrap.setOperator(dto.getOperator());
            scrap.setScrapTime(now);
            scrap.setAgingBatchId(savedBatch.getId());
            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append("[老化批次归档] 批次号: ").append(savedBatch.getBatchNo());
            if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
                remarkBuilder.append(" | ").append(dto.getRemark());
            }
            if (itemDTO.getRemark() != null && !itemDTO.getRemark().isEmpty()) {
                remarkBuilder.append(" | ").append(itemDTO.getRemark());
            }
            scrap.setRemark(remarkBuilder.toString());
            scrapRecordRepository.save(scrap);
        }

        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
        return savedBatch;
    }

    @Override
    @Transactional
    public AgingBatch update(AgingBatchUpdateDTO dto) {
        AgingBatch batch = agingBatchRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        if (dto.getReason() != null) {
            batch.setReason(dto.getReason());
        }
        if (dto.getOperator() != null) {
            batch.setOperator(dto.getOperator());
        }
        if (dto.getRemark() != null) {
            batch.setRemark(dto.getRemark());
        }

        return agingBatchRepository.save(batch);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AgingBatch batch = agingBatchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        List<AgingBatchItem> items = agingBatchItemRepository.findByBatchId(id);
        for (AgingBatchItem item : items) {
            accessoryService.addStock(item.getAccessoryId(), item.getScrapQuantity());
        }

        scrapRecordRepository.deleteByAgingBatchId(id);

        agingBatchItemRepository.deleteByBatchId(id);
        agingBatchRepository.deleteById(id);

        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
    }

    @Override
    public AgingBatch getById(Long id) {
        return agingBatchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<AgingBatch> list() {
        return agingBatchRepository.findAll();
    }

    @Override
    public List<AgingBatchItem> getItemsByBatchId(Long batchId) {
        return agingBatchItemRepository.findByBatchId(batchId);
    }
}
