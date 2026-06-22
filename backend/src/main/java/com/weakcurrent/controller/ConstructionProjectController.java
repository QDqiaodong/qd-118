package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.ConstructionProjectCreateDTO;
import com.weakcurrent.dto.ConstructionProjectUpdateDTO;
import com.weakcurrent.dto.ProjectConsumptionDTO;
import com.weakcurrent.entity.ConstructionProject;
import com.weakcurrent.service.ConstructionProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/construction-projects")
@RequiredArgsConstructor
public class ConstructionProjectController {

    private final ConstructionProjectService constructionProjectService;

    @PostMapping
    public Result<ConstructionProject> create(@Valid @RequestBody ConstructionProjectCreateDTO dto) {
        return Result.success(constructionProjectService.create(dto));
    }

    @PutMapping
    public Result<ConstructionProject> update(@Valid @RequestBody ConstructionProjectUpdateDTO dto) {
        return Result.success(constructionProjectService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        constructionProjectService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ConstructionProject> getById(@PathVariable Long id) {
        return Result.success(constructionProjectService.getById(id));
    }

    @GetMapping
    public Result<List<ConstructionProject>> list() {
        return Result.success(constructionProjectService.list());
    }

    @GetMapping("/active")
    public Result<List<ConstructionProject>> listActive() {
        return Result.success(constructionProjectService.listActive());
    }

    @GetMapping("/{id}/consumption")
    public Result<List<ProjectConsumptionDTO>> getProjectConsumption(@PathVariable Long id) {
        return Result.success(constructionProjectService.getProjectConsumptionSummary(id));
    }

    @GetMapping("/consumption")
    public Result<List<ProjectConsumptionDTO>> getAllProjectConsumption() {
        return Result.success(constructionProjectService.getAllProjectConsumptionSummary());
    }

    @PatchMapping("/{id}/status")
    public Result<ConstructionProject> updateStatus(@PathVariable Long id, @RequestParam Boolean status) {
        ConstructionProject project = constructionProjectService.getById(id);
        ConstructionProjectUpdateDTO dto = new ConstructionProjectUpdateDTO();
        dto.setId(id);
        dto.setProjectNo(project.getProjectNo());
        dto.setProjectName(project.getProjectName());
        dto.setFactoryArea(project.getFactoryArea());
        dto.setStatus(status);
        dto.setRemark(project.getRemark());
        return Result.success(constructionProjectService.update(dto));
    }
}
