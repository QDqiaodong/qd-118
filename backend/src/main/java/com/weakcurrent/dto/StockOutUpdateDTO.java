package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockOutUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    private String workshop;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    @NotNull(message = "出库时间不能为空")
    private LocalDateTime outTime;

    private String remark;
}
