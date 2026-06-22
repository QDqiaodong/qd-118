package com.weakcurrent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ScrapItemDTO {

    @NotNull(message = "配件ID不能为空")
    private Long accessoryId;

    private String accessoryName;

    private String accessoryModel;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "报废数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "报废原因不能为空")
    private String reason;

    private String reasonGroup;

    private String remark;

    private Long agingPreCheckId;

    @Valid
    private List<ScrapAttachmentDTO> attachments;
}
