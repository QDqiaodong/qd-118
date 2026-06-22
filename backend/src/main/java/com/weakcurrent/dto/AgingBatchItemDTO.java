package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgingBatchItemDTO {

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    private String accessoryModel;

    private String unit;

    private String warehouseZone;

    private Long categoryId;

    private String categoryName;

    private Integer stockQuantity;

    @NotNull(message = "报废数量不能为空")
    @Min(value = 1, message = "报废数量必须大于0")
    private Integer scrapQuantity;

    private java.time.LocalDateTime inboundTime;

    private String reason;

    private String remark;
}
