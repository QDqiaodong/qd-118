package com.weakcurrent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgingBatchGenerateDTO {

    private Long categoryId;

    private String warehouseZone;

    private LocalDateTime inboundStart;

    private LocalDateTime inboundEnd;
}
