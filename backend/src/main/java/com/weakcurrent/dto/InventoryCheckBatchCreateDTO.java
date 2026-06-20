package com.weakcurrent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InventoryCheckBatchCreateDTO {

    @NotBlank(message = "清点人不能为空")
    private String checkPerson;

    private LocalDateTime checkTime;

    private String remark;

    @Valid
    @NotEmpty(message = "清点明细不能为空")
    private List<InventoryCheckBatchItem> details;

    @Data
    public static class InventoryCheckBatchItem {

        @NotNull(message = "配件ID不能为空")
        private Long accessoryId;

        private Integer systemQuantity;

        @NotNull(message = "实盘数量不能为空")
        @Min(value = 0, message = "实盘数量不能小于0")
        private Integer actualQuantity;

        private Integer diff;
    }
}
