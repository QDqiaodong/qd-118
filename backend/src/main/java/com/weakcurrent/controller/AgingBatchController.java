package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.AgingBatchCreateDTO;
import com.weakcurrent.dto.AgingBatchGenerateDTO;
import com.weakcurrent.dto.AgingBatchItemDTO;
import com.weakcurrent.dto.AgingBatchUpdateDTO;
import com.weakcurrent.entity.AgingBatch;
import com.weakcurrent.entity.AgingBatchItem;
import com.weakcurrent.service.AgingBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aging-batches")
@RequiredArgsConstructor
public class AgingBatchController {

    private final AgingBatchService agingBatchService;

    @PostMapping("/preview")
    public Result<List<AgingBatchItemDTO>> generatePreview(@RequestBody AgingBatchGenerateDTO dto) {
        return Result.success(agingBatchService.generatePreview(dto));
    }

    @PostMapping
    public Result<AgingBatch> create(@Valid @RequestBody AgingBatchCreateDTO dto) {
        return Result.success(agingBatchService.create(dto));
    }

    @PutMapping
    public Result<AgingBatch> update(@Valid @RequestBody AgingBatchUpdateDTO dto) {
        return Result.success(agingBatchService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        agingBatchService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AgingBatch> getById(@PathVariable Long id) {
        return Result.success(agingBatchService.getById(id));
    }

    @GetMapping
    public Result<List<AgingBatch>> list() {
        return Result.success(agingBatchService.list());
    }

    @GetMapping("/{id}/items")
    public Result<List<AgingBatchItem>> getItemsByBatchId(@PathVariable Long id) {
        return Result.success(agingBatchService.getItemsByBatchId(id));
    }
}
