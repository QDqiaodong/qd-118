package com.weakcurrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessoryDuplicateDTO {

    private Long id;

    private String name;

    private String model;

    private String spec;

    private String warehouseZone;
}
