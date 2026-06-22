package com.weakcurrent.repository;

import com.weakcurrent.entity.ScrapAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapAttachmentRepository extends JpaRepository<ScrapAttachment, Long> {

    List<ScrapAttachment> findByScrapRecordId(Long scrapRecordId);

    void deleteByScrapRecordId(Long scrapRecordId);
}
