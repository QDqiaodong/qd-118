package com.weakcurrent.service;

import com.weakcurrent.dto.AccessoryCreateDTO;
import com.weakcurrent.dto.AccessoryUpdateDTO;
import com.weakcurrent.entity.Accessory;

import java.util.List;

public interface AccessoryService {

    Accessory create(AccessoryCreateDTO dto);

    Accessory update(AccessoryUpdateDTO dto);

    void delete(Long id);

    Accessory getById(Long id);

    List<Accessory> list();

    List<Accessory> listByCategoryId(Long categoryId);

    List<Accessory> searchByName(String name);

    void deductStock(Long id, Integer quantity);

    void addStock(Long id, Integer quantity);

    String buildCategoryPath(Long categoryId);

    void syncCategoryPath(Long categoryId, String categoryName, String categoryPath);

    void syncCategoryPathForDescendants(List<Long> categoryIds, String categoryName, String categoryPath);
}
