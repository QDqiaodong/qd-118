package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.ScrapCreateDTO;
import com.weakcurrent.dto.ScrapUpdateDTO;
import com.weakcurrent.entity.ScrapRecord;
import com.weakcurrent.service.ScrapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scrap-records")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping
    public Result<ScrapRecord> create(@Valid @RequestBody ScrapCreateDTO dto) {
        return Result.success(scrapService.create(dto));
    }

    @PutMapping
    public Result<ScrapRecord> update(@Valid @RequestBody ScrapUpdateDTO dto) {
        return Result.success(scrapService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scrapService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ScrapRecord> getById(@PathVariable Long id) {
        return Result.success(scrapService.getById(id));
    }

    @GetMapping
    public Result<List<ScrapRecord>> list() {
        return Result.success(scrapService.list());
    }

    @GetMapping("/accessory/{accessoryId}")
    public Result<List<ScrapRecord>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(scrapService.listByAccessoryId(accessoryId));
    }
}
