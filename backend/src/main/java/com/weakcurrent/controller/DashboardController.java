package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.repository.StockOutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AccessoryRepository accessoryRepository;
    private final StockOutRepository stockOutRepository;
    private final ScrapRecordRepository scrapRecordRepository;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long accessoryCount = accessoryRepository.count();
        stats.put("accessoryCount", accessoryCount);

        Integer totalStock = accessoryRepository.sumAllStockQuantity();
        stats.put("totalStock", totalStock != null ? totalStock : 0);

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        Integer monthStockOut = stockOutRepository.sumQuantityByOutTimeBetween(monthStart, now);
        stats.put("monthStockOut", monthStockOut != null ? monthStockOut : 0);

        Integer monthScrap = scrapRecordRepository.sumQuantityByScrapTimeBetween(monthStart, now);
        stats.put("monthScrap", monthScrap != null ? monthScrap : 0);

        return Result.success(stats);
    }

    @GetMapping("/top5")
    public Result<List<Accessory>> getTop5() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "stockQuantity"));
        List<Accessory> top5 = accessoryRepository.findAll(pageRequest).getContent();
        return Result.success(top5);
    }
}
