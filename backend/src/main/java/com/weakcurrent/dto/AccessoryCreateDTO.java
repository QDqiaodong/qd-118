package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccessoryCreateDTO {

    @NotBlank(message = "配件名称不能为空")
    private String name;

    private String model;

    private String spec;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stockQuantity;

    private String warehouseZone;

    private String unit;
}
