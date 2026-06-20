package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrap_record")
public class ScrapRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "accessory_model", length = 100)
    private String accessoryModel;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "scrap_time", nullable = false)
    private LocalDateTime scrapTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = scrapTime != null ? scrapTime : LocalDateTime.now();
        }
    }
}
