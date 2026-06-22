package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aging_pre_check")
public class AgingPreCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "accessory_model", length = 100)
    private String accessoryModel;

    @Column(name = "warehouse_zone", length = 50)
    private String warehouseZone;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "yellowing_level", nullable = false)
    private Integer yellowingLevel = 0;

    @Column(name = "cracking_level", nullable = false)
    private Integer crackingLevel = 0;

    @Column(name = "oxidation_level", nullable = false)
    private Integer oxidationLevel = 0;

    @Column(name = "overall_level", nullable = false)
    private Integer overallLevel = 0;

    @Column(name = "threshold_reached", nullable = false)
    private Boolean thresholdReached = false;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "scrap_record_id")
    private Long scrapRecordId;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = checkTime != null ? checkTime : LocalDateTime.now();
        }
    }
}
