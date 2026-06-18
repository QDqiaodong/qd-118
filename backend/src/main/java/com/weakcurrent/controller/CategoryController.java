package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.CategoryCreateDTO;
import com.weakcurrent.dto.CategoryUpdateDTO;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<AccessoryCategory> create(@Valid @RequestBody CategoryCreateDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @PutMapping
    public Result<AccessoryCategory> update(@Valid @RequestBody CategoryUpdateDTO dto) {
        return Result.success(categoryService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AccessoryCategory> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @GetMapping
    public Result<List<AccessoryCategory>> list() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/tree")
    public Result<List<AccessoryCategory>> getTree() {
        return Result.success(categoryService.getTree());
    }
}
