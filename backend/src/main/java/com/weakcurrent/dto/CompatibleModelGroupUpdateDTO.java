package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompatibleModelGroupUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "兼容组名称不能为空")
    private String groupName;

    private String brand;

    @NotBlank(message = "型号不能为空")
    private String model;

    private String spec;

    private Long accessoryId;

    private String remark;
}
