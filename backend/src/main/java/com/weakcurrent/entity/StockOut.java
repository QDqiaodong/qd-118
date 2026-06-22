package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_out")
public class StockOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "workshop", length = 100)
    private String workshop;

    @Column(name = "usage_id", nullable = false)
    private Long usageId;

    @Column(name = "usage_name", length = 100)
    private String usageName;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_no", length = 64)
    private String projectNo;

    @Column(name = "purpose", length = 500)
    private String purpose;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "out_time", nullable = false)
    private LocalDateTime outTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
