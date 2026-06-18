package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryCheckUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    @NotNull(message = "实际数量不能为空")
    @Min(value = 0, message = "实际数量不能小于0")
    private Integer physicalQuantity;

    @NotNull(message = "系统数量不能为空")
    @Min(value = 0, message = "系统数量不能小于0")
    private Integer systemQuantity;

    @NotNull(message = "差异数量不能为空")
    private Integer difference;

    @NotBlank(message = "清点人不能为空")
    private String checkPerson;

    @NotNull(message = "清点时间不能为空")
    private LocalDateTime checkTime;

    private String remark;
}
