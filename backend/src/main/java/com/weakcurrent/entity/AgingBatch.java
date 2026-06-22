package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aging_batch")
public class AgingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no", nullable = false, length = 50, unique = true)
    private String batchNo;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "warehouse_zone", length = 50)
    private String warehouseZone;

    @Column(name = "inbound_start")
    private LocalDateTime inboundStart;

    @Column(name = "inbound_end")
    private LocalDateTime inboundEnd;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount = 0;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "archive_time", nullable = false)
    private LocalDateTime archiveTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = archiveTime != null ? archiveTime : LocalDateTime.now();
        }
    }
}
