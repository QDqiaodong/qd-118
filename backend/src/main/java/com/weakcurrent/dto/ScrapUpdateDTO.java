package com.weakcurrent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScrapUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "报废数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "报废原因不能为空")
    private String reason;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    @NotNull(message = "报废时间不能为空")
    private LocalDateTime scrapTime;

    private String remark;
}
