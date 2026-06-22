package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConstructionProjectCreateDTO {

    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    private String factoryArea;

    private Boolean status = true;

    private String remark;
}
