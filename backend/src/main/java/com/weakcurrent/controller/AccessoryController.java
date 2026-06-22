package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.AccessoryCreateDTO;
import com.weakcurrent.dto.AccessorySpecTemplateDTO;
import com.weakcurrent.dto.AccessoryUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.service.AccessoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accessories")
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @PostMapping
    public Result<Accessory> create(@Valid @RequestBody AccessoryCreateDTO dto) {
        return Result.success(accessoryService.create(dto));
    }

    @PutMapping
    public Result<Accessory> update(@Valid @RequestBody AccessoryUpdateDTO dto) {
        return Result.success(accessoryService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accessoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Accessory> getById(@PathVariable Long id) {
        return Result.success(accessoryService.getById(id));
    }

    @GetMapping
    public Result<List<Accessory>> list() {
        return Result.success(accessoryService.list());
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<Accessory>> listByCategoryId(@PathVariable Long categoryId) {
        return Result.success(accessoryService.listByCategoryId(categoryId));
    }

    @GetMapping("/search")
    public Result<List<Accessory>> searchByName(@RequestParam String name) {
        return Result.success(accessoryService.searchByName(name));
    }

    @PostMapping("/{id}/deduct-stock")
    public Result<Void> deductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        accessoryService.deductStock(id, quantity);
        return Result.success();
    }

    @PostMapping("/{id}/add-stock")
    public Result<Void> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        accessoryService.addStock(id, quantity);
        return Result.success();
    }

    @GetMapping("/spec-template")
    public Result<List<AccessorySpecTemplateDTO>> getAllSpecTemplates() {
        return Result.success(accessoryService.getAllSpecTemplates());
    }

    @GetMapping("/spec-template/{categoryCode}")
    public Result<AccessorySpecTemplateDTO> getSpecTemplateByCategoryCode(@PathVariable String categoryCode) {
        return Result.success(accessoryService.getSpecTemplateByCategoryCode(categoryCode));
    }
}
