package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "accessory")
public class Accessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "spec", length = 100)
    private String spec;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "category_path", length = 500)
    private String categoryPath;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "warehouse_zone", length = 50)
    private String warehouseZone;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "square_number", length = 20)
    private String squareNumber;

    @Column(name = "pin_count")
    private Integer pinCount;

    @Column(name = "width", length = 20)
    private String width;

    @Column(name = "height", length = 20)
    private String height;

    @Column(name = "diameter", length = 20)
    private String diameter;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
