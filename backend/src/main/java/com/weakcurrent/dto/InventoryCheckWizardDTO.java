package com.weakcurrent.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InventoryCheckWizardDTO {

    @NotEmpty(message = "库区不能为空")
    private String warehouseZone;

    @NotNull(message = "清点人不能为空")
    private String checkPerson;

    private LocalDateTime checkTime;

    private String remark;

    @NotEmpty(message = "清点明细不能为空")
    private List<InventoryCheckWizardItem> items;

    @Data
    public static class InventoryCheckWizardItem {
        @NotNull(message = "配件ID不能为空")
        private Long accessoryId;

        private String accessoryName;

        private String categoryName;

        private Integer systemQuantity;

        @NotNull(message = "实盘数量不能为空")
        private Integer physicalQuantity;

        private Integer difference;
    }
}
