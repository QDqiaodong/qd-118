package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.WarehouseZoneStatsDTO;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.ScrapRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final AccessoryRepository accessoryRepository;
    private final ScrapRecordRepository scrapRecordRepository;

    @GetMapping("/zone-stats")
    public Result<List<WarehouseZoneStatsDTO>> getZoneStats() {
        List<Object[]> accessoryStats = accessoryRepository.countAndSumByWarehouseZone();
        List<Object[]> scrapStats = scrapRecordRepository.sumScrapQuantityByWarehouseZone();

        Map<String, Integer> scrapMap = new HashMap<>();
        for (Object[] row : scrapStats) {
            String zone = (String) row[0];
            Integer quantity = ((Number) row[1]).intValue();
            scrapMap.put(zone, quantity);
        }

        List<WarehouseZoneStatsDTO> result = new ArrayList<>();
        for (Object[] row : accessoryStats) {
            String zone = (String) row[0];
            Integer count = ((Number) row[1]).intValue();
            Integer totalStock = ((Number) row[2]).intValue();
            Integer scrapQuantity = scrapMap.getOrDefault(zone, 0);

            double scrapRatio = 0.0;
            if (totalStock + scrapQuantity > 0) {
                scrapRatio = (double) scrapQuantity / (totalStock + scrapQuantity) * 100;
            }

            WarehouseZoneStatsDTO dto = new WarehouseZoneStatsDTO();
            dto.setZone(zone);
            dto.setAccessoryCount(count);
            dto.setTotalStock(totalStock);
            dto.setScrapQuantity(scrapQuantity);
            dto.setScrapRatio(Math.round(scrapRatio * 100.0) / 100.0);

            result.add(dto);
        }

        return Result.success(result);
    }
}
