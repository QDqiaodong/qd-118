package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AccessoryRepository accessoryRepository;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/top5")
    public Result<List<Accessory>> getTop5() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "stockQuantity"));
        List<Accessory> top5 = accessoryRepository.findAll(pageRequest).getContent();
        return Result.success(top5);
    }
}
