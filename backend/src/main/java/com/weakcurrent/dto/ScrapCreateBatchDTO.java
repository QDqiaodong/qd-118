package com.weakcurrent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ScrapCreateBatchDTO {

    @NotEmpty(message = "报废项不能为空")
    @Valid
    private List<ScrapItemDTO> items;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    private String remark;
}
