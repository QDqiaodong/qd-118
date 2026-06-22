package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "aging_batch_item")
public class AgingBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "accessory_model", length = 100)
    private String accessoryModel;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "warehouse_zone", length = 50)
    private String warehouseZone;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "scrap_quantity", nullable = false)
    private Integer scrapQuantity;

    @Column(name = "inbound_time")
    private java.time.LocalDateTime inboundTime;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "remark", length = 500)
    private String remark;
}
