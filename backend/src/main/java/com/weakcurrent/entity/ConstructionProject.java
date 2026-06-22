package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "construction_project")
public class ConstructionProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_no", nullable = false, length = 64, unique = true)
    private String projectNo;

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(name = "factory_area", length = 100)
    private String factoryArea;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
