package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkshopUsageUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "用途名称不能为空")
    @Size(max = 100, message = "用途名称长度不能超过100个字符")
    private String name;

    @Size(max = 32, message = "用途编码长度不能超过32个字符")
    private String code;

    private Integer sort = 0;

    private Boolean enabled = true;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
