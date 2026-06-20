package com.weakcurrent.service.impl;

import com.weakcurrent.dto.TrunkSpecDTO;
import com.weakcurrent.dto.ZoneInventoryStatsDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.repository.AccessoryCategoryRepository;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.service.TrunkSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrunkSpecServiceImpl implements TrunkSpecService {

    private final AccessoryRepository accessoryRepository;
    private final AccessoryCategoryRepository accessoryCategoryRepository;

    private static final Long PVC_TRUNK_CATEGORY_ID = 7L;
    private static final Long METAL_TRAY_CATEGORY_ID = 8L;
    private static final Long CORRUGATED_PIPE_CATEGORY_ID = 9L;

    private static final Long TERMINAL_PARENT_ID = 1L;
    private static final Long TRUNK_PARENT_ID = 2L;
    private static final Long CLIP_PARENT_ID = 3L;

    private static final Pattern DIMENSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX*]\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern DIAMETER_PATTERN = Pattern.compile("[Φφ]?\\s*(\\d+(?:\\.\\d+)?)\\s*mm");

    @Override
    public List<TrunkSpecDTO> getTrunkSpecCompare() {
        List<TrunkSpecDTO> result = new ArrayList<>();

        result.add(buildTrunkSpecDTO(PVC_TRUNK_CATEGORY_ID, "PVC方形线槽", "pvc"));
        result.add(buildTrunkSpecDTO(METAL_TRAY_CATEGORY_ID, "金属桥架", "metal"));
        result.add(buildTrunkSpecDTO(CORRUGATED_PIPE_CATEGORY_ID, "波纹管", "corrugated"));

        return result;
    }

    private TrunkSpecDTO buildTrunkSpecDTO(Long categoryId, String categoryName, String categoryType) {
        TrunkSpecDTO dto = new TrunkSpecDTO();
        dto.setCategoryName(categoryName);
        dto.setCategoryType(categoryType);

        List<Accessory> accessories = accessoryRepository.findByCategoryId(categoryId);
        List<TrunkSpecDTO.TrunkSpecItem> items = accessories.stream()
                .map(this::convertToTrunkSpecItem)
                .collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }

    private TrunkSpecDTO.TrunkSpecItem convertToTrunkSpecItem(Accessory accessory) {
        TrunkSpecDTO.TrunkSpecItem item = new TrunkSpecDTO.TrunkSpecItem();
        item.setId(accessory.getId());
        item.setName(accessory.getName());
        item.setModel(accessory.getModel());
        item.setSpec(accessory.getSpec());
        item.setUnit(accessory.getUnit());
        item.setStockQuantity(accessory.getStockQuantity());
        item.setWarehouseZone(accessory.getWarehouseZone());

        String spec = accessory.getSpec() != null ? accessory.getSpec() : "";

        Matcher dimensionMatcher = DIMENSION_PATTERN.matcher(spec);
        if (dimensionMatcher.find()) {
            item.setWidth(dimensionMatcher.group(1) + "mm");
            item.setHeight(dimensionMatcher.group(2) + "mm");
        }

        Matcher diameterMatcher = DIAMETER_PATTERN.matcher(spec);
        if (diameterMatcher.find()) {
            item.setDiameter("Φ" + diameterMatcher.group(1) + "mm");
        }

        return item;
    }

    @Override
    public ZoneInventoryStatsDTO getZoneInventoryStats(String zone) {
        List<Accessory> accessories = accessoryRepository.findByWarehouseZoneStartingWith(zone);

        Set<Long> terminalCategoryIds = getChildCategoryIds(TERMINAL_PARENT_ID);
        Set<Long> trunkCategoryIds = getChildCategoryIds(TRUNK_PARENT_ID);
        Set<Long> clipCategoryIds = getChildCategoryIds(CLIP_PARENT_ID);

        ZoneInventoryStatsDTO dto = new ZoneInventoryStatsDTO();
        dto.setWarehouseZone(zone);
        dto.setTotalItemCount(accessories.size());

        int terminalCount = 0;
        int trunkCount = 0;
        int clipCount = 0;
        int otherCount = 0;
        int unfilledCount = 0;

        List<ZoneInventoryStatsDTO.ZoneInventoryItem> items = new ArrayList<>();

        for (Accessory accessory : accessories) {
            ZoneInventoryStatsDTO.ZoneInventoryItem item = new ZoneInventoryStatsDTO.ZoneInventoryItem();
            item.setId(accessory.getId());
            item.setName(accessory.getName());
            item.setModel(accessory.getModel());
            item.setSpec(accessory.getSpec());
            item.setCategoryName(accessory.getCategoryName());
            item.setSystemQuantity(accessory.getStockQuantity());
            item.setPhysicalQuantity(null);
            item.setUnit(accessory.getUnit());
            item.setFilled(false);

            Long categoryId = accessory.getCategoryId();
            if (terminalCategoryIds.contains(categoryId)) {
                item.setCategoryType("terminal");
                terminalCount++;
            } else if (trunkCategoryIds.contains(categoryId)) {
                item.setCategoryType("trunk");
                trunkCount++;
            } else if (clipCategoryIds.contains(categoryId)) {
                item.setCategoryType("clip");
                clipCount++;
            } else {
                item.setCategoryType("other");
                otherCount++;
            }

            items.add(item);
        }

        dto.setTerminalCount(terminalCount);
        dto.setTrunkCount(trunkCount);
        dto.setClipCount(clipCount);
        dto.setOtherCount(otherCount);
        dto.setUnfilledCount(accessories.size());
        dto.setItems(items);

        return dto;
    }

    private Set<Long> getChildCategoryIds(Long parentId) {
        Set<Long> ids = new HashSet<>();
        List<AccessoryCategory> children = accessoryCategoryRepository.findByParentId(parentId);
        for (AccessoryCategory child : children) {
            ids.add(child.getId());
        }
        return ids;
    }

    @Override
    public List<String> getAllZones() {
        List<Object[]> stats = accessoryRepository.countAndSumByWarehouseZone();
        Set<String> zonePrefixes = new TreeSet<>();

        for (Object[] row : stats) {
            String zone = (String) row[0];
            if (zone != null && !zone.isEmpty()) {
                String prefix = zone.substring(0, 1);
                zonePrefixes.add(prefix);
            }
        }

        return new ArrayList<>(zonePrefixes);
    }
}
