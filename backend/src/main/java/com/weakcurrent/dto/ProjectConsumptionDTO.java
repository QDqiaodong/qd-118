package com.weakcurrent.dto;

import lombok.Data;

@Data
public class ProjectConsumptionDTO {

    private Long projectId;

    private String projectNo;

    private String projectName;

    private String factoryArea;

    private Long totalQuantity;

    private Integer recordCount;

    private String workshop;

    private String purpose;

    private String accessoryName;

    private Integer quantity;

    private String outTime;
}
