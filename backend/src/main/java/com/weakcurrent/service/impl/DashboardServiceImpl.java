package com.weakcurrent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.repository.ScrapRecordRepository;
import com.weakcurrent.repository.StockOutRepository;
import com.weakcurrent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String KEY_INVENTORY = "dashboard:stats:inventory";
    private static final String KEY_MONTH_STOCK_OUT = "dashboard:stats:month-stock-out";
    private static final String KEY_MONTH_SCRAP = "dashboard:stats:month-scrap";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final AccessoryRepository accessoryRepository;
    private final StockOutRepository stockOutRepository;
    private final ScrapRecordRepository scrapRecordRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.putAll(getInventoryOverview());
        stats.put("monthStockOut", getMonthStockOut());
        stats.put("monthScrap", getMonthScrap());
        return stats;
    }

    @Override
    public void evictInventoryOverview() {
        evictAfterCommit(KEY_INVENTORY);
    }

    @Override
    public void evictMonthStockOut() {
        evictAfterCommit(KEY_MONTH_STOCK_OUT);
    }

    @Override
    public void evictMonthScrap() {
        evictAfterCommit(KEY_MONTH_SCRAP);
    }

    private Map<String, Object> getInventoryOverview() {
        String cached = stringRedisTemplate.opsForValue().get(KEY_INVENTORY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("读取库存概览缓存失败，回源查询: {}", e.getMessage());
            }
        }

        long accessoryCount = accessoryRepository.count();
        Integer totalStock = accessoryRepository.sumAllStockQuantity();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("accessoryCount", accessoryCount);
        overview.put("totalStock", totalStock != null ? totalStock : 0);

        cacheJson(KEY_INVENTORY, overview);
        return overview;
    }

    private Integer getMonthStockOut() {
        String cached = stringRedisTemplate.opsForValue().get(KEY_MONTH_STOCK_OUT);
        if (cached != null) {
            try {
                return Integer.parseInt(cached);
            } catch (NumberFormatException e) {
                log.warn("本月领用缓存格式异常，回源查询: {}", e.getMessage());
            }
        }

        LocalDateTime[] range = currentMonthRange();
        Integer value = stockOutRepository.sumQuantityByOutTimeBetween(range[0], range[1]);
        int result = value != null ? value : 0;
        stringRedisTemplate.opsForValue().set(KEY_MONTH_STOCK_OUT, String.valueOf(result), CACHE_TTL);
        return result;
    }

    private Integer getMonthScrap() {
        String cached = stringRedisTemplate.opsForValue().get(KEY_MONTH_SCRAP);
        if (cached != null) {
            try {
                return Integer.parseInt(cached);
            } catch (NumberFormatException e) {
                log.warn("本月报废缓存格式异常，回源查询: {}", e.getMessage());
            }
        }

        LocalDateTime[] range = currentMonthRange();
        Integer value = scrapRecordRepository.sumQuantityByScrapTimeBetween(range[0], range[1]);
        int result = value != null ? value : 0;
        stringRedisTemplate.opsForValue().set(KEY_MONTH_SCRAP, String.valueOf(result), CACHE_TTL);
        return result;
    }

    private void cacheJson(String key, Object value) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入缓存失败 key={}: {}", key, e.getMessage());
        }
    }

    private void evictAfterCommit(String key) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    stringRedisTemplate.delete(key);
                }
            });
        } else {
            stringRedisTemplate.delete(key);
        }
    }

    private LocalDateTime[] currentMonthRange() {
        LocalDateTime start = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return new LocalDateTime[]{start, LocalDateTime.now()};
    }
}
