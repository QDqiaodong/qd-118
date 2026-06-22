package com.weakcurrent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessoryWithLatestCheckDTO {

    private Long id;
    private String name;
    private String model;
    private String spec;
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    private Integer stockQuantity;
    private String warehouseZone;
    private String unit;
    private String squareNumber;
    private Integer pinCount;
    private String width;
    private String height;
    private String diameter;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Long latestCheckId;
    private Integer latestPhysicalQuantity;
    private Integer latestSystemQuantity;
    private Integer latestDifference;
    private String latestCheckStatus;
    private String latestCheckPerson;
    private LocalDateTime latestCheckTime;
    private String latestCheckRemark;
}
