package com.weakcurrent.service;

import com.weakcurrent.dto.CategoryCreateDTO;
import com.weakcurrent.dto.CategoryUpdateDTO;
import com.weakcurrent.entity.AccessoryCategory;

import java.util.List;

public interface CategoryService {

    AccessoryCategory create(CategoryCreateDTO dto);

    AccessoryCategory update(CategoryUpdateDTO dto);

    void delete(Long id);

    AccessoryCategory getById(Long id);

    List<AccessoryCategory> list();

    List<AccessoryCategory> getTree();
}
