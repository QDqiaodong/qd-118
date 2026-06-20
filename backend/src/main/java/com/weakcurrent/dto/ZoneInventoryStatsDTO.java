package com.weakcurrent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ZoneInventoryStatsDTO {

    private String warehouseZone;

    private Integer totalItemCount;

    private Integer terminalCount;

    private Integer trunkCount;

    private Integer clipCount;

    private Integer otherCount;

    private Integer unfilledCount;

    private List<ZoneInventoryItem> items;

    @Data
    public static class ZoneInventoryItem {
        private Long id;
        private String name;
        private String model;
        private String spec;
        private String categoryName;
        private String categoryType;
        private Integer systemQuantity;
        private Integer physicalQuantity;
        private String unit;
        private Boolean filled;
    }
}
