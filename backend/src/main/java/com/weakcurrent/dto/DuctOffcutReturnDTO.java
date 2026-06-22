package com.weakcurrent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DuctOffcutReturnDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    private String returnZone;

    private String operator;

    private String remark;
}
