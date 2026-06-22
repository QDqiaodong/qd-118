package com.weakcurrent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgingPreCheckUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    @NotNull(message = "发黄等级不能为空")
    @Min(value = 0, message = "发黄等级最小为0")
    @Max(value = 5, message = "发黄等级最大为5")
    private Integer yellowingLevel;

    @NotNull(message = "脆裂等级不能为空")
    @Min(value = 0, message = "脆裂等级最小为0")
    @Max(value = 5, message = "脆裂等级最大为5")
    private Integer crackingLevel;

    @NotNull(message = "金属氧化等级不能为空")
    @Min(value = 0, message = "金属氧化等级最小为0")
    @Max(value = 5, message = "金属氧化等级最大为5")
    private Integer oxidationLevel;

    @Min(value = 0, message = "综合等级最小为0")
    @Max(value = 5, message = "综合等级最大为5")
    private Integer overallLevel;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    @NotNull(message = "检查时间不能为空")
    private LocalDateTime checkTime;

    private String remark;
}
