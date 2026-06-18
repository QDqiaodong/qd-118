package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "父级ID不能为空")
    private Long parentId;

    private Integer sort;
}
