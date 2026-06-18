package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.StockOutCreateDTO;
import com.weakcurrent.dto.StockOutUpdateDTO;
import com.weakcurrent.entity.StockOut;
import com.weakcurrent.service.StockOutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-outs")
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService stockOutService;

    @PostMapping
    public Result<StockOut> create(@Valid @RequestBody StockOutCreateDTO dto) {
        return Result.success(stockOutService.create(dto));
    }

    @PutMapping
    public Result<StockOut> update(@Valid @RequestBody StockOutUpdateDTO dto) {
        return Result.success(stockOutService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockOutService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<StockOut> getById(@PathVariable Long id) {
        return Result.success(stockOutService.getById(id));
    }

    @GetMapping
    public Result<List<StockOut>> list() {
        return Result.success(stockOutService.list());
    }

    @GetMapping("/accessory/{accessoryId}")
    public Result<List<StockOut>> listByAccessoryId(@PathVariable Long accessoryId) {
        return Result.success(stockOutService.listByAccessoryId(accessoryId));
    }
}
