package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.AgingPreCheckBatchDTO;
import com.weakcurrent.dto.AgingPreCheckCreateDTO;
import com.weakcurrent.dto.AgingPreCheckUpdateDTO;
import com.weakcurrent.entity.AgingPreCheck;
import com.weakcurrent.service.AgingPreCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aging-pre-checks")
@RequiredArgsConstructor
public class AgingPreCheckController {

    private final AgingPreCheckService agingPreCheckService;

    @PostMapping
    public Result<AgingPreCheck> create(@Valid @RequestBody AgingPreCheckCreateDTO dto) {
        return Result.success(agingPreCheckService.create(dto));
    }

    @PostMapping("/batch")
    public Result<List<AgingPreCheck>> createBatch(@Valid @RequestBody AgingPreCheckBatchDTO dto) {
        return Result.success(agingPreCheckService.createBatch(dto));
    }

    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculateLevel(@RequestBody Map<String, Integer> levels) {
        Integer yellowing = levels.get("yellowingLevel");
        Integer cracking = levels.get("crackingLevel");
        Integer oxidation = levels.get("oxidationLevel");

        Integer overallLevel = agingPreCheckService.calculateOverallLevel(yellowing, cracking, oxidation);
        boolean thresholdReached = agingPreCheckService.checkThresholdReached(overallLevel);

        Map<String, Object> result = new HashMap<>();
        result.put("overallLevel", overallLevel);
        result.put("thresholdReached", thresholdReached);

        return Result.success(result);
    }

    @PutMapping
    public Result<AgingPreCheck> update(@Valid @RequestBody AgingPreCheckUpdateDTO dto) {
        return Result.success(agingPreCheckService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        agingPreCheckService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AgingPreCheck> getById(@PathVariable Long id) {
        return Result.success(agingPreCheckService.getById(id));
    }

    @GetMapping
    public Result<List<AgingPreCheck>> list() {
        return Result.success(agingPreCheckService.list());
    }

    @GetMapping("/accessory/{accessoryId}")
    public Result<List<AgingPreCheck>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(agingPreCheckService.listByAccessoryId(accessoryId));
    }

    @GetMapping("/pending-scrap")
    public Result<List<AgingPreCheck>> listPendingScrap() {
        return Result.success(agingPreCheckService.listPendingScrap());
    }
}
