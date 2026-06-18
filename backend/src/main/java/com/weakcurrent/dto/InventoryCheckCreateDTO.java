package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryCheckCreateDTO {

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    @NotNull(message = "实际数量不能为空")
    @Min(value = 0, message = "实际数量不能小于0")
    private Integer physicalQuantity;

    private Integer systemQuantity;

    private Integer difference;

    @NotBlank(message = "清点人不能为空")
    private String checkPerson;

    private LocalDateTime checkTime;

    private String remark;
}
