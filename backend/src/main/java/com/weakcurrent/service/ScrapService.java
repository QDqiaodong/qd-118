package com.weakcurrent.service;

import com.weakcurrent.dto.ScrapAttachmentDTO;
import com.weakcurrent.dto.ScrapCreateBatchDTO;
import com.weakcurrent.dto.ScrapCreateDTO;
import com.weakcurrent.dto.ScrapUpdateDTO;
import com.weakcurrent.entity.ScrapAttachment;
import com.weakcurrent.entity.ScrapRecord;

import java.util.List;

public interface ScrapService {

    ScrapRecord create(ScrapCreateDTO dto);

    List<ScrapRecord> createBatch(ScrapCreateBatchDTO dto);

    ScrapRecord update(ScrapUpdateDTO dto);

    void delete(Long id);

    ScrapRecord getById(Long id);

    List<ScrapRecord> list();

    List<ScrapRecord> listByAccessoryId(Long accessoryId);

    List<ScrapAttachment> getAttachmentsByScrapId(Long scrapId);
}
