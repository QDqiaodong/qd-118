package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.WorkshopUsageCreateDTO;
import com.weakcurrent.dto.WorkshopUsageUpdateDTO;
import com.weakcurrent.entity.WorkshopUsage;
import com.weakcurrent.service.WorkshopUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workshop-usages")
@RequiredArgsConstructor
public class WorkshopUsageController {

    private final WorkshopUsageService workshopUsageService;

    @PostMapping
    public Result<WorkshopUsage> create(@Valid @RequestBody WorkshopUsageCreateDTO dto) {
        return Result.success(workshopUsageService.create(dto));
    }

    @PutMapping
    public Result<WorkshopUsage> update(@Valid @RequestBody WorkshopUsageUpdateDTO dto) {
        return Result.success(workshopUsageService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workshopUsageService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<WorkshopUsage> getById(@PathVariable Long id) {
        return Result.success(workshopUsageService.getById(id));
    }

    @GetMapping
    public Result<List<WorkshopUsage>> list() {
        return Result.success(workshopUsageService.list());
    }

    @GetMapping("/enabled")
    public Result<List<WorkshopUsage>> listEnabled() {
        return Result.success(workshopUsageService.listEnabled());
    }

    @PatchMapping("/{id}/status")
    public Result<WorkshopUsage> updateStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        return Result.success(workshopUsageService.updateStatus(id, enabled));
    }
}
