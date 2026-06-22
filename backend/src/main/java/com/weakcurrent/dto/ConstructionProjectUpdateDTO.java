package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConstructionProjectUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    private String factoryArea;

    private Boolean status;

    private String remark;
}
