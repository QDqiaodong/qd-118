package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.ScrapCreateDTO;
import com.weakcurrent.dto.ScrapUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.ScrapRecord;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRecordRepository scrapRecordRepository;
    private final AccessoryService accessoryService;

    @Override
    @Transactional
    public ScrapRecord create(ScrapCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        accessoryService.deductStock(dto.getAccessoryId(), dto.getQuantity());

        ScrapRecord scrap = new ScrapRecord();
        scrap.setAccessoryId(dto.getAccessoryId());
        scrap.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        scrap.setQuantity(dto.getQuantity());
        scrap.setReason(dto.getReason());
        scrap.setOperator(dto.getOperator());
        scrap.setScrapTime(dto.getScrapTime() != null ? dto.getScrapTime() : LocalDateTime.now());
        scrap.setRemark(dto.getRemark());

        return scrapRecordRepository.save(scrap);
    }

    @Override
    @Transactional
    public ScrapRecord update(ScrapUpdateDTO dto) {
        ScrapRecord scrap = scrapRecordRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        int quantityDiff = dto.getQuantity() - scrap.getQuantity();
        if (quantityDiff > 0) {
            accessoryService.deductStock(dto.getAccessoryId(), quantityDiff);
        } else if (quantityDiff < 0) {
            accessoryService.addStock(dto.getAccessoryId(), -quantityDiff);
        }

        scrap.setAccessoryId(dto.getAccessoryId());
        scrap.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        scrap.setQuantity(dto.getQuantity());
        scrap.setReason(dto.getReason());
        scrap.setOperator(dto.getOperator());
        scrap.setScrapTime(dto.getScrapTime());
        scrap.setRemark(dto.getRemark());

        return scrapRecordRepository.save(scrap);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ScrapRecord scrap = scrapRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        accessoryService.addStock(scrap.getAccessoryId(), scrap.getQuantity());

        scrapRecordRepository.deleteById(id);
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
