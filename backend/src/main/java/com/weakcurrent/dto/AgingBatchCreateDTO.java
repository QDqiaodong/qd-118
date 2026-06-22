package com.weakcurrent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgingBatchCreateDTO {

    private Long categoryId;

    private String categoryName;

    private String warehouseZone;

    private LocalDateTime inboundStart;

    private LocalDateTime inboundEnd;

    @NotBlank(message = "报废原因不能为空")
    private String reason;

    @NotBlank(message = "归档人不能为空")
    private String operator;

    private String remark;

    @NotEmpty(message = "批次明细不能为空")
    @Valid
    private List<AgingBatchItemDTO> items;
}
