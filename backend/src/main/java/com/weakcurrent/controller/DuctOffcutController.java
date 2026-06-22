package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.DuctOffcutCreateDTO;
import com.weakcurrent.dto.DuctOffcutReturnDTO;
import com.weakcurrent.entity.DuctOffcut;
import com.weakcurrent.service.DuctOffcutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duct-offcuts")
@RequiredArgsConstructor
public class DuctOffcutController {

    private final DuctOffcutService ductOffcutService;

    @PostMapping
    public Result<DuctOffcut> create(@Valid @RequestBody DuctOffcutCreateDTO dto) {
        return Result.success(ductOffcutService.create(dto));
    }

    @PostMapping("/return")
    public Result<DuctOffcut> returnToStock(@Valid @RequestBody DuctOffcutReturnDTO dto) {
        return Result.success(ductOffcutService.returnToStock(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ductOffcutService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DuctOffcut> getById(@PathVariable Long id) {
        return Result.success(ductOffcutService.getById(id));
    }

    @GetMapping
    public Result<List<DuctOffcut>> list() {
        return Result.success(ductOffcutService.list());
    }

    @GetMapping("/by-accessory/{accessoryId}")
    public Result<List<DuctOffcut>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(ductOffcutService.listByAccessoryId(accessoryId));
    }

    @GetMapping("/by-status/{status}")
    public Result<List<DuctOffcut>> listByStatus(@PathVariable Integer status) {
        return Result.success(ductOffcutService.listByStatus(status));
    }
}
