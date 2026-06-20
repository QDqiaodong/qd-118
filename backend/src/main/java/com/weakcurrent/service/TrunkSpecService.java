package com.weakcurrent.service;

import com.weakcurrent.dto.TrunkSpecDTO;
import com.weakcurrent.dto.ZoneInventoryStatsDTO;

import java.util.List;

public interface TrunkSpecService {

    List<TrunkSpecDTO> getTrunkSpecCompare();

    ZoneInventoryStatsDTO getZoneInventoryStats(String zone);

    List<String> getAllZones();
}
