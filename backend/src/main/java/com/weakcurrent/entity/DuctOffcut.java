package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "duct_offcut")
public class DuctOffcut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "accessory_model", length = 100)
    private String accessoryModel;

    @Column(name = "stock_out_id")
    private Long stockOutId;

    @Column(name = "original_length", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalLength;

    @Column(name = "used_length", nullable = false, precision = 10, scale = 2)
    private BigDecimal usedLength;

    @Column(name = "offcut_length", nullable = false, precision = 10, scale = 2)
    private BigDecimal offcutLength;

    @Column(name = "return_zone", length = 50)
    private String returnZone;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
