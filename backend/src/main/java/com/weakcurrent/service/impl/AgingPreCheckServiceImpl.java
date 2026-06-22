package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.AgingPreCheckBatchDTO;
import com.weakcurrent.dto.AgingPreCheckCreateDTO;
import com.weakcurrent.dto.AgingPreCheckUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AgingPreCheck;
import com.weakcurrent.repository.AgingPreCheckRepository;
import com.weakcurrent.service.AccessoryService;
import com.weakcurrent.service.AgingPreCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgingPreCheckServiceImpl implements AgingPreCheckService {

    private final AgingPreCheckRepository agingPreCheckRepository;
    private final AccessoryService accessoryService;

    private static final int THRESHOLD_LEVEL = 3;

    @Override
    @Transactional
    public AgingPreCheck create(AgingPreCheckCreateDTO dto) {
        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        Integer overallLevel = dto.getOverallLevel() != null ? dto.getOverallLevel()
                : calculateOverallLevel(dto.getYellowingLevel(), dto.getCrackingLevel(), dto.getOxidationLevel());
        boolean thresholdReached = checkThresholdReached(overallLevel);

        AgingPreCheck check = new AgingPreCheck();
        check.setAccessoryId(dto.getAccessoryId());
        check.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        check.setAccessoryModel(accessory.getModel());
        check.setWarehouseZone(accessory.getWarehouseZone());
        check.setCategoryName(accessory.getCategoryName());
        check.setUnit(accessory.getUnit());
        check.setStockQuantity(accessory.getStockQuantity());
        check.setYellowingLevel(dto.getYellowingLevel());
        check.setCrackingLevel(dto.getCrackingLevel());
        check.setOxidationLevel(dto.getOxidationLevel());
        check.setOverallLevel(overallLevel);
        check.setThresholdReached(thresholdReached);
        check.setOperator(dto.getOperator());
        check.setCheckTime(dto.getCheckTime() != null ? dto.getCheckTime() : LocalDateTime.now());
        check.setRemark(dto.getRemark());

        return agingPreCheckRepository.save(check);
    }

    @Override
    @Transactional
    public List<AgingPreCheck> createBatch(AgingPreCheckBatchDTO dto) {
        List<AgingPreCheck> results = new ArrayList<>();
        LocalDateTime checkTime = dto.getCheckTime() != null ? dto.getCheckTime() : LocalDateTime.now();

        for (AgingPreCheckCreateDTO item : dto.getItems()) {
            Accessory accessory = accessoryService.getById(item.getAccessoryId());

            Integer overallLevel = item.getOverallLevel() != null ? item.getOverallLevel()
                    : calculateOverallLevel(item.getYellowingLevel(), item.getCrackingLevel(), item.getOxidationLevel());
            boolean thresholdReached = checkThresholdReached(overallLevel);

            AgingPreCheck check = new AgingPreCheck();
            check.setAccessoryId(item.getAccessoryId());
            check.setAccessoryName(item.getAccessoryName() != null && !item.getAccessoryName().isEmpty()
                    ? item.getAccessoryName() : accessory.getName());
            check.setAccessoryModel(accessory.getModel());
            check.setWarehouseZone(accessory.getWarehouseZone());
            check.setCategoryName(accessory.getCategoryName());
            check.setUnit(accessory.getUnit());
            check.setStockQuantity(accessory.getStockQuantity());
            check.setYellowingLevel(item.getYellowingLevel());
            check.setCrackingLevel(item.getCrackingLevel());
            check.setOxidationLevel(item.getOxidationLevel());
            check.setOverallLevel(overallLevel);
            check.setThresholdReached(thresholdReached);
            check.setOperator(dto.getOperator());
            check.setCheckTime(checkTime);

            StringBuilder remarkBuilder = new StringBuilder();
            if (item.getRemark() != null && !item.getRemark().isEmpty()) {
                remarkBuilder.append(item.getRemark());
            }
            if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
                if (remarkBuilder.length() > 0) {
                    remarkBuilder.append(" | ");
                }
                remarkBuilder.append(dto.getRemark());
            }
            check.setRemark(remarkBuilder.length() > 0 ? remarkBuilder.toString() : null);

            results.add(agingPreCheckRepository.save(check));
        }

        return results;
    }

    @Override
    @Transactional
    public AgingPreCheck update(AgingPreCheckUpdateDTO dto) {
        AgingPreCheck check = agingPreCheckRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Accessory accessory = accessoryService.getById(dto.getAccessoryId());

        Integer overallLevel = dto.getOverallLevel() != null ? dto.getOverallLevel()
                : calculateOverallLevel(dto.getYellowingLevel(), dto.getCrackingLevel(), dto.getOxidationLevel());
        boolean thresholdReached = checkThresholdReached(overallLevel);

        check.setAccessoryId(dto.getAccessoryId());
        check.setAccessoryName(dto.getAccessoryName() != null && !dto.getAccessoryName().isEmpty()
                ? dto.getAccessoryName() : accessory.getName());
        check.setAccessoryModel(accessory.getModel());
        check.setWarehouseZone(accessory.getWarehouseZone());
        check.setCategoryName(accessory.getCategoryName());
        check.setUnit(accessory.getUnit());
        check.setStockQuantity(accessory.getStockQuantity());
        check.setYellowingLevel(dto.getYellowingLevel());
        check.setCrackingLevel(dto.getCrackingLevel());
        check.setOxidationLevel(dto.getOxidationLevel());
        check.setOverallLevel(overallLevel);
        check.setThresholdReached(thresholdReached);
        check.setOperator(dto.getOperator());
        check.setCheckTime(dto.getCheckTime());
        check.setRemark(dto.getRemark());

        return agingPreCheckRepository.save(check);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!agingPreCheckRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        agingPreCheckRepository.deleteById(id);
    }

    @Override
    public AgingPreCheck getById(Long id) {
        return agingPreCheckRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<AgingPreCheck> list() {
        return agingPreCheckRepository.findAll();
    }

    @Override
    public List<AgingPreCheck> listByAccessoryId(Long accessoryId) {
        return agingPreCheckRepository.findByAccessoryId(accessoryId);
    }

    @Override
    public List<AgingPreCheck> listPendingScrap() {
        return agingPreCheckRepository.findByScrapRecordIdIsNullAndThresholdReachedTrue();
    }

    @Override
    public Integer calculateOverallLevel(Integer yellowing, Integer cracking, Integer oxidation) {
        int sum = (yellowing != null ? yellowing : 0)
                + (cracking != null ? cracking : 0)
                + (oxidation != null ? oxidation : 0);
        return (int) Math.ceil(sum / 3.0);
    }

    @Override
    public boolean checkThresholdReached(Integer overallLevel) {
        return overallLevel != null && overallLevel >= THRESHOLD_LEVEL;
    }
}
