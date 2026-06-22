package com.weakcurrent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgingPreCheckBatchDTO {

    @NotEmpty(message = "预检项不能为空")
    @Valid
    private List<AgingPreCheckCreateDTO> items;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    private LocalDateTime checkTime;

    private String remark;
}
