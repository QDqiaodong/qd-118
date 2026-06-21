package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.StockOutCreateDTO;
import com.weakcurrent.dto.StockOutUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.StockOut;
import com.weakcurrent.repository.StockOutRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.StockOutService;
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

    @Override
    @Transactional
    public StockOut create(StockOutCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());

        StockOut stockOut = new StockOut();
        stockOut.setAccessoryId(dto.getAccessoryId());
        stockOut.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        stockOut.setWorkshop(dto.getWorkshop());
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setOperator(dto.getOperator());
        stockOut.setOutTime(dto.getOutTime() != null ? dto.getOutTime() : LocalDateTime.now());
        stockOut.setRemark(dto.getRemark());

        return stockOutRepository.save(stockOut);
    }

    @Override
    @Transactional
    public StockOut update(StockOutUpdateDTO dto) {
        StockOut stockOut = stockOutRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        Long oldAccessoryId = stockOut.getAccessoryId();
        Integer oldQuantity = stockOut.getQuantity();

        if (!dto.getAccessoryId().equals(oldAccessoryId)) {
            // 配件变更：先回滚原配件库存（恢复原出库数量），再按新数量扣减新配件库存
            accessoryService.addStock(oldAccessoryId, oldQuantity);
            accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());
        } else {
            // 配件未变更：按数量差异调整库存
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
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setOperator(dto.getOperator());
        stockOut.setOutTime(dto.getOutTime());
        stockOut.setRemark(dto.getRemark());

        return stockOutRepository.save(stockOut);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StockOut stockOut = stockOutRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessoryService.addStock(stockOut.getAccessoryId(), stockOut.getQuantity());

        stockOutRepository.deleteById(id);
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
