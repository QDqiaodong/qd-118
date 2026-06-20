package com.weakcurrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseZoneStatsDTO {

    private String zone;

    private Integer accessoryCount;

    private Integer totalStock;

    private Integer scrapQuantity;

    private Double scrapRatio;
}
