package com.weakcurrent.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "编码只能包含字母、数字、下划线和中划线")
    private String code;

    @NotNull(message = "父级ID不能为空")
    private Long parentId;

    @JsonAlias("sortOrder")
    private Integer sort;

    private Boolean enabled;

    private String remark;
}
