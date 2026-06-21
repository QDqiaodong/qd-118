package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventory_check")
public class InventoryCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accessory_id", nullable = false)
    private Long accessoryId;

    @Column(name = "accessory_name", length = 100)
    private String accessoryName;

    @Column(name = "physical_quantity", nullable = false)
    private Integer physicalQuantity;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "difference", nullable = false)
    private Integer difference;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "check_person", length = 50)
    private String checkPerson;

    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
