package com.weakcurrent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategorySortItem {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
