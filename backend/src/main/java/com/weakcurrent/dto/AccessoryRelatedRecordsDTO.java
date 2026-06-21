package com.weakcurrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessoryRelatedRecordsDTO {

    private Integer stockOutCount;

    private Integer inventoryCheckCount;

    private Integer scrapCount;

    public Integer getTotalCount() {
        return (stockOutCount == null ? 0 : stockOutCount)
                + (inventoryCheckCount == null ? 0 : inventoryCheckCount)
                + (scrapCount == null ? 0 : scrapCount);
    }
}
