package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.ScrapCreateBatchDTO;
import com.weakcurrent.dto.ScrapCreateDTO;
import com.weakcurrent.dto.ScrapItemDTO;
import com.weakcurrent.dto.ScrapUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.ScrapRecord;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.DashboardService;
import com.weakcurrent.service.ScrapService;
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
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRecordRepository scrapRecordRepository;
    private final AccessoryService accessoryService;
    private final DashboardService dashboardService;

    @Override
    @Transactional
    public ScrapRecord create(ScrapCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        if (accessory.getStockQuantity() < dto.getQuantity()) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "配件【" + accessory.getName() + "】报废数量 " + dto.getQuantity() + " 超过库存 " + accessory.getStockQuantity());
        }

        accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());

        ScrapRecord scrap = new ScrapRecord();
        scrap.setAccessoryId(dto.getAccessoryId());
        scrap.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        scrap.setAccessoryModel(accessory.getModel());
        scrap.setUnit(accessory.getUnit());
        scrap.setQuantity(dto.getQuantity());
        scrap.setReason(dto.getReason());
        scrap.setOperator(dto.getOperator());
        scrap.setScrapTime(dto.getScrapTime() != null ? dto.getScrapTime() : LocalDateTime.now());
        scrap.setRemark(dto.getRemark());

        ScrapRecord saved = scrapRecordRepository.save(scrap);
        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
        return saved;
    }

    @Override
    @Transactional
    public List<ScrapRecord> createBatch(ScrapCreateBatchDTO dto) {
        Map<Long, Integer> mergedQuantities = new HashMap<>();
        for (ScrapItemDTO item : dto.getItems()) {
            Long accessoryId = item.getAccessoryId();
            Integer quantity = item.getQuantity();
            mergedQuantities.merge(accessoryId, quantity, Integer::sum);
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

        List<ScrapRecord> records = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (ScrapItemDTO item : dto.getItems()) {
            Accessory accessory = accessoryCache.get(item.getAccessoryId());

            ScrapRecord scrap = new ScrapRecord();
            scrap.setAccessoryId(item.getAccessoryId());
            scrap.setAccessoryName(item.getAccessoryName() != null && !item.getAccessoryName().isEmpty()
                    ? item.getAccessoryName() : accessory.getName());
            scrap.setAccessoryModel(item.getAccessoryModel() != null && !item.getAccessoryModel().isEmpty()
                    ? item.getAccessoryModel() : accessory.getModel());
            scrap.setUnit(accessory.getUnit());
            scrap.setQuantity(item.getQuantity());
            scrap.setReason(item.getReason());
            scrap.setOperator(dto.getOperator());
            scrap.setScrapTime(now);

            StringBuilder remarkBuilder = new StringBuilder();
            if (item.getReasonGroup() != null && !item.getReasonGroup().isEmpty()) {
                remarkBuilder.append("[").append(item.getReasonGroup()).append("] ");
            }
            if (item.getRemark() != null && !item.getRemark().isEmpty()) {
                remarkBuilder.append(item.getRemark());
            }
            if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
                if (remarkBuilder.length() > 0) {
                    remarkBuilder.append(" | ");
                }
                remarkBuilder.append(dto.getRemark());
            }
            scrap.setRemark(remarkBuilder.length() > 0 ? remarkBuilder.toString() : null);

            records.add(scrapRecordRepository.save(scrap));
        }

        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
        return records;
    }

    @Override
    @Transactional
    public ScrapRecord update(ScrapUpdateDTO dto) {
        ScrapRecord scrap = scrapRecordRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        Long oldAccessoryId = scrap.getAccessoryId();
        Integer oldQuantity = scrap.getQuantity();

        if (!dto.getAccessoryId().equals(oldAccessoryId)) {
            // 配件变更：先回滚原配件库存（恢复原报废数量），再校验并按新数量扣减新配件库存
            accessoryService.addStock(oldAccessoryId, oldQuantity);
            if (accessory.getStockQuantity() < dto.getQuantity()) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "配件【" + accessory.getName() + "】报废数量 " + dto.getQuantity() + " 超过库存 " + accessory.getStockQuantity());
            }
            accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());
        } else {
            // 配件未变更：按数量差异调整库存
            int quantityDiff = dto.getQuantity() - oldQuantity;
            if (quantityDiff > 0) {
                if (accessory.getStockQuantity() < quantityDiff) {
                    throw new BusinessException(ResultCode.BAD_REQUEST,
                            "配件【" + accessory.getName() + "】增加报废数量 " + quantityDiff + " 超过库存 " + accessory.getStockQuantity());
                }
                accessoryService.deductStock(dto.getAccessoryId(), quantityDiff);
            } else if (quantityDiff < 0) {
                accessoryService.addStock(dto.getAccessoryId(), -quantityDiff);
            }
        }

        scrap.setAccessoryId(dto.getAccessoryId());
        scrap.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        scrap.setAccessoryModel(accessory.getModel());
        scrap.setUnit(accessory.getUnit());
        scrap.setQuantity(dto.getQuantity());
        scrap.setReason(dto.getReason());
        scrap.setOperator(dto.getOperator());
        scrap.setScrapTime(dto.getScrapTime());
        scrap.setRemark(dto.getRemark());

        ScrapRecord saved = scrapRecordRepository.save(scrap);
        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ScrapRecord scrap = scrapRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessoryService.addStock(scrap.getAccessoryId(), scrap.getQuantity());

        scrapRecordRepository.deleteById(id);
        dashboardService.evictMonthScrap();
        dashboardService.evictInventoryOverview();
    }

    @Override
    public ScrapRecord getById(Long id) {
        return scrapRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<ScrapRecord> list() {
        return scrapRecordRepository.findAll();
    }

    @Override
    public List<ScrapRecord> listByAccessoryId(Long accessoryId) {
        return scrapRecordRepository.findByAccessoryId(accessoryId);
    }
}
