package com.weakcurrent.service;

import com.weakcurrent.dto.AgingBatchCreateDTO;
import com.weakcurrent.dto.AgingBatchGenerateDTO;
import com.weakcurrent.dto.AgingBatchItemDTO;
import com.weakcurrent.dto.AgingBatchUpdateDTO;
import com.weakcurrent.entity.AgingBatch;
import com.weakcurrent.entity.AgingBatchItem;

import java.util.List;

public interface AgingBatchService {

    List<AgingBatchItemDTO> generatePreview(AgingBatchGenerateDTO dto);

    AgingBatch create(AgingBatchCreateDTO dto);

    AgingBatch update(AgingBatchUpdateDTO dto);

    void delete(Long id);

    AgingBatch getById(Long id);

    List<AgingBatch> list();

    List<AgingBatchItem> getItemsByBatchId(Long batchId);
}
