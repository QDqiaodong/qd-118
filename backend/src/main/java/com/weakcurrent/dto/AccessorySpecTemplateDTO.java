package com.weakcurrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessorySpecTemplateDTO {

    private Long categoryId;

    private String categoryName;

    private String categoryCode;

    private List<SpecField> specFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecField {
        private String fieldName;
        private String fieldLabel;
        private String fieldType;
        private String placeholder;
    }
}
