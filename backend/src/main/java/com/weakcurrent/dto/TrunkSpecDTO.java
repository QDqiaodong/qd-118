package com.weakcurrent.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrunkSpecDTO {

    private String categoryName;

    private String categoryType;

    private List<TrunkSpecItem> items;

    @Data
    public static class TrunkSpecItem {
        private Long id;
        private String name;
        private String model;
        private String spec;
        private String width;
        private String height;
        private String diameter;
        private String unit;
        private Integer stockQuantity;
        private String warehouseZone;
    }
}
