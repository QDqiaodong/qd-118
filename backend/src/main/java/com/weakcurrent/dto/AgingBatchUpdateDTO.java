package com.weakcurrent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgingBatchUpdateDTO {

    @NotNull(message = "批次ID不能为空")
    private Long id;

    private String reason;

    private String operator;

    private String remark;
}
