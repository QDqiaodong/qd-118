package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.CompatibleModelGroupCreateDTO;
import com.weakcurrent.dto.CompatibleModelGroupUpdateDTO;
import com.weakcurrent.dto.CompatibleModelGroupValidateDTO;
import com.weakcurrent.dto.CompatibleModelGroupValidateResultDTO;
import com.weakcurrent.entity.CompatibleModelGroup;
import com.weakcurrent.service.CompatibleModelGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compatible-model-groups")
@RequiredArgsConstructor
public class CompatibleModelGroupController {

    private final CompatibleModelGroupService compatibleModelGroupService;

    @PostMapping("/validate")
    public Result<CompatibleModelGroupValidateResultDTO> validate(@Valid @RequestBody CompatibleModelGroupValidateDTO dto) {
        return Result.success(compatibleModelGroupService.validate(dto));
    }

    @PostMapping
    public Result<CompatibleModelGroup> create(@Valid @RequestBody CompatibleModelGroupCreateDTO dto) {
        return Result.success(compatibleModelGroupService.create(dto));
    }

    @PutMapping
    public Result<CompatibleModelGroup> update(@Valid @RequestBody CompatibleModelGroupUpdateDTO dto) {
        return Result.success(compatibleModelGroupService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        compatibleModelGroupService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CompatibleModelGroup> getById(@PathVariable Long id) {
        return Result.success(compatibleModelGroupService.getById(id));
    }

    @GetMapping
    public Result<List<CompatibleModelGroup>> list() {
        return Result.success(compatibleModelGroupService.list());
    }

    @GetMapping("/by-group/{groupName}")
    public Result<List<CompatibleModelGroup>> listByGroupName(@PathVariable String groupName) {
        return Result.success(compatibleModelGroupService.listByGroupName(groupName));
    }

    @GetMapping("/by-model")
    public Result<List<CompatibleModelGroup>> listByModel(@RequestParam String model) {
        return Result.success(compatibleModelGroupService.listByModel(model));
    }

    @GetMapping("/by-accessory/{accessoryId}")
    public Result<List<CompatibleModelGroup>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(compatibleModelGroupService.listByAccessoryId(accessoryId));
    }

    @GetMapping("/group-names")
    public Result<List<String>> listGroupNames() {
        return Result.success(compatibleModelGroupService.listGroupNames());
    }
}
