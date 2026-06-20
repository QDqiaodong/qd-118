package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.InventoryCheckCreateDTO;
import com.weakcurrent.dto.InventoryCheckUpdateDTO;
import com.weakcurrent.dto.InventoryCheckWizardDTO;
import com.weakcurrent.entity.InventoryCheck;
import com.weakcurrent.service.InventoryCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory-checks")
@RequiredArgsConstructor
public class InventoryCheckController {

    private final InventoryCheckService inventoryCheckService;

    @PostMapping
    public Result<InventoryCheck> create(@Valid @RequestBody InventoryCheckCreateDTO dto) {
        return Result.success(inventoryCheckService.create(dto));
    }

    @PostMapping("/wizard-batch")
    public Result<List<InventoryCheck>> createWizardBatch(@Valid @RequestBody InventoryCheckWizardDTO dto) {
        return Result.success(inventoryCheckService.createWizardBatch(dto));
    }

    @PutMapping
    public Result<InventoryCheck> update(@Valid @RequestBody InventoryCheckUpdateDTO dto) {
        return Result.success(inventoryCheckService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryCheckService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<InventoryCheck> getById(@PathVariable Long id) {
        return Result.success(inventoryCheckService.getById(id));
    }

    @GetMapping
    public Result<List<InventoryCheck>> list() {
        return Result.success(inventoryCheckService.list());
    }

    @GetMapping("/accessory/{accessoryId}")
    public Result<List<InventoryCheck>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(inventoryCheckService.listByAccessoryId(accessoryId));
    }
}
