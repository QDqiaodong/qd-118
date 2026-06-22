package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.StockOutCreateDTO;
import com.weakcurrent.dto.StockOutUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.StockOut;
import com.weakcurrent.repository.StockOutRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.DashboardService;
import com.weakcurrent.service.StockOutService;
import com.weakcurrent.service.WorkshopUsageService;
import com.weakcurrent.entity.WorkshopUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOutServiceImpl implements StockOutService {

    private final StockOutRepository stockOutRepository;
    private final AccessoryService accessoryService;
    private final DashboardService dashboardService;
    private final WorkshopUsageService workshopUsageService;

    @Override
    @Transactional
    public StockOut create(StockOutCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());
        WorkshopUsage usage = workshopUsageService.getById(dto.getUsageId());

        accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());

        StockOut stockOut = new StockOut();
        stockOut.setAccessoryId(dto.getAccessoryId());
        stockOut.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        stockOut.setWorkshop(dto.getWorkshop());
        stockOut.setUsageId(dto.getUsageId());
        stockOut.setUsageName(usage.getName());
        stockOut.setProjectId(dto.getProjectId());
        stockOut.setProjectNo(dto.getProjectNo());
        stockOut.setPurpose(dto.getPurpose());
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setOperator(dto.getOperator());
        stockOut.setOutTime(dto.getOutTime() != null ? dto.getOutTime() : LocalDateTime.now());
        stockOut.setRemark(dto.getRemark());

        StockOut saved = stockOutRepository.save(stockOut);
        dashboardService.evictMonthStockOut();
        dashboardService.evictInventoryOverview();
        return saved;
    }

    @Override
    @Transactional
    public StockOut update(StockOutUpdateDTO dto) {
        StockOut stockOut = stockOutRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());
        WorkshopUsage usage = workshopUsageService.getById(dto.getUsageId());

        Long oldAccessoryId = stockOut.getAccessoryId();
        Integer oldQuantity = stockOut.getQuantity();

        if (!dto.getAccessoryId().equals(oldAccessoryId)) {
            accessoryService.addStock(oldAccessoryId, oldQuantity);
            accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());
        } else {
            int quantityDiff = dto.getQuantity() - oldQuantity;
            if (quantityDiff > 0) {
                accessoryService.deductStock(dto.getAccessoryId(), quantityDiff);
            } else if (quantityDiff < 0) {
                accessoryService.addStock(dto.getAccessoryId(), -quantityDiff);
            }
        }

        stockOut.setAccessoryId(dto.getAccessoryId());
        stockOut.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        stockOut.setWorkshop(dto.getWorkshop());
        stockOut.setUsageId(dto.getUsageId());
        stockOut.setUsageName(usage.getName());
        stockOut.setProjectId(dto.getProjectId());
        stockOut.setProjectNo(dto.getProjectNo());
        stockOut.setPurpose(dto.getPurpose());
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setOperator(dto.getOperator());
        stockOut.setOutTime(dto.getOutTime());
        stockOut.setRemark(dto.getRemark());

        StockOut saved = stockOutRepository.save(stockOut);
        dashboardService.evictMonthStockOut();
        dashboardService.evictInventoryOverview();
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StockOut stockOut = stockOutRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessoryService.addStock(stockOut.getAccessoryId(), stockOut.getQuantity());

        stockOutRepository.deleteById(id);
        dashboardService.evictMonthStockOut();
        dashboardService.evictInventoryOverview();
    }

    @Override
    public StockOut getById(Long id) {
        return stockOutRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<StockOut> list() {
        return stockOutRepository.findAll();
    }

    @Override
    public List<StockOut> listByAccessoryId(Long accessoryId) {
        return stockOutRepository.findByAccessoryId(accessoryId);
    }
}
