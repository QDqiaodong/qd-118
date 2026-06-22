package com.weakcurrent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrap_attachment")
public class ScrapAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrap_record_id", nullable = false)
    private Long scrapRecordId;

    @Column(name = "file_url", length = 500, nullable = false)
    private String fileUrl;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
