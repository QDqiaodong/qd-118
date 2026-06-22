package com.weakcurrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompatibleModelGroupValidateResultDTO {

    private boolean valid;

    private boolean duplicate;

    private String message;

    private String normalizedGroupName;

    private String normalizedBrand;

    private String normalizedModel;

    public static CompatibleModelGroupValidateResultDTO success(String groupName, String brand, String model) {
        return new CompatibleModelGroupValidateResultDTO(true, false, null, groupName, brand, model);
    }

    public static CompatibleModelGroupValidateResultDTO duplicate(String message) {
        return new CompatibleModelGroupValidateResultDTO(false, true, message, null, null, null);
    }

    public static CompatibleModelGroupValidateResultDTO invalid(String message) {
        return new CompatibleModelGroupValidateResultDTO(false, false, message, null, null, null);
    }
}
