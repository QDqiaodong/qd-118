package com.weakcurrent.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getStats();

    void evictInventoryOverview();

    void evictMonthStockOut();

    void evictMonthScrap();
}
