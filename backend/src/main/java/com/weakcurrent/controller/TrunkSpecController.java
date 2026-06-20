package com.weakcurrent.controller;

import com.weakcurrent.common.Result;
import com.weakcurrent.dto.TrunkSpecDTO;
import com.weakcurrent.dto.ZoneInventoryStatsDTO;
import com.weakcurrent.service.TrunkSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trunk-spec")
@RequiredArgsConstructor
public class TrunkSpecController {

    private final TrunkSpecService trunkSpecService;

    @GetMapping("/compare")
    public Result<List<TrunkSpecDTO>> getTrunkSpecCompare() {
        return Result.success(trunkSpecService.getTrunkSpecCompare());
    }

    @GetMapping("/zone-inventory/{zone}")
    public Result<ZoneInventoryStatsDTO> getZoneInventoryStats(@PathVariable String zone) {
        return Result.success(trunkSpecService.getZoneInventoryStats(zone));
    }

    @GetMapping("/zones")
    public Result<List<String>> getAllZones() {
        return Result.success(trunkSpecService.getAllZones());
    }
}
