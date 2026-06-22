package com.weakcurrent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DuctOffcutCreateDTO {

    @NotNull(message = "线槽配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    private String accessoryModel;

    private Long stockOutId;

    @NotNull(message = "领用原始长度不能为空")
    private BigDecimal originalLength;

    @NotNull(message = "已使用长度不能为空")
    private BigDecimal usedLength;

    @NotNull(message = "余料长度不能为空")
    private BigDecimal offcutLength;

    private String returnZone;

    private String operator;

    private String remark;
}
