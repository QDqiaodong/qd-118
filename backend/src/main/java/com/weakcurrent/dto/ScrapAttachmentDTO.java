package com.weakcurrent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapAttachmentDTO {

    @NotBlank(message = "附件地址不能为空")
    private String fileUrl;

    private String fileName;

    private String fileType;

    private String description;
}
