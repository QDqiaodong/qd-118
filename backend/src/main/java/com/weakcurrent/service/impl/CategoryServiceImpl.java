package com.weakcurrent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.CategoryCreateDTO;
import com.weakcurrent.dto.CategorySortItem;
import com.weakcurrent.dto.CategoryUpdateDTO;
import com.weakcurrent.entity.Accessory;
import com.weakcurrent.entity.AccessoryCategory;
import com.weakcurrent.repository.AccessoryCategoryRepository;
import com.weakcurrent.repository.AccessoryRepository;
import com.weakcurrent.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_TREE_KEY = "category:tree";
    private static final long CACHE_EXPIRE_HOURS = 1;

    private final AccessoryCategoryRepository categoryRepository;
    private final AccessoryRepository accessoryRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AccessoryCategory create(CategoryCreateDTO dto) {
        AccessoryCategory category = new AccessoryCategory();
        category.setName(dto.getName());
        category.setCode(dto.getCode());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        category.setRemark(dto.getRemark());

        AccessoryCategory saved = categoryRepository.save(category);
        clearCache();
        return saved;
    }

    @Override
    @Transactional
    public AccessoryCategory update(CategoryUpdateDTO dto) {
        AccessoryCategory category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        Long newParentId = dto.getParentId();
        if (newParentId != null && !newParentId.equals(0L)) {
            List<AccessoryCategory> allCategories = categoryRepository.findAllByOrderBySortAsc();
            Set<Long> descendantIds = collectDescendantIds(dto.getId(), allCategories);
            if (descendantIds.contains(newParentId)) {
                throw new BusinessException(ResultCode.CATEGORY_CIRCULAR_REFERENCE);
            }
        }

        category.setName(dto.getName());
        category.setCode(dto.getCode());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        category.setRemark(dto.getRemark());

        AccessoryCategory saved = categoryRepository.save(category);
        clearCache();
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
        clearCache();
    }

    @Override
    public AccessoryCategory getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<AccessoryCategory> list() {
        return categoryRepository.findAllByOrderBySortAsc();
    }

    @Override
    public List<AccessoryCategory> getTree() {
        String cacheValue = stringRedisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        if (cacheValue != null && !cacheValue.isEmpty()) {
            try {
                List<AccessoryCategory> cachedTree = objectMapper.readValue(cacheValue, new TypeReference<List<AccessoryCategory>>() {});
                log.debug("从Redis缓存获取分类树数据");
                return cachedTree;
            } catch (Exception e) {
                log.warn("解析Redis缓存分类树数据失败，将从数据库重新加载", e);
            }
        }

        List<AccessoryCategory> allCategories = categoryRepository.findAllByOrderBySortAsc();
        List<AccessoryCategory> tree = buildTree(allCategories);

        try {
            String jsonValue = objectMapper.writeValueAsString(tree);
            stringRedisTemplate.opsForValue().set(CATEGORY_TREE_KEY, jsonValue, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("分类树数据已缓存到Redis");
        } catch (Exception e) {
            log.warn("缓存分类树数据到Redis失败", e);
        }

        return tree;
    }

    @Override
    @Transactional
    public void updateSort(List<CategorySortItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<AccessoryCategory> toUpdate = new ArrayList<>();
        for (CategorySortItem item : items) {
            AccessoryCategory category = categoryRepository.findById(item.getId())
                    .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
            category.setSort(item.getSort());
            toUpdate.add(category);
        }
        categoryRepository.saveAll(toUpdate);
        clearCache();
    }

    @Override
    @Transactional
    public AccessoryCategory updateStatus(Long id, Boolean enabled) {
        AccessoryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
        category.setEnabled(enabled != null ? enabled : true);
        AccessoryCategory saved = categoryRepository.save(category);
        clearCache();
        return saved;
    }

    @Override
    public List<Accessory> getArchivePreview(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        List<AccessoryCategory> allCategories = categoryRepository.findAllByOrderBySortAsc();
        Set<Long> categoryIds = collectDescendantIds(categoryId, allCategories);
        if (categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return accessoryRepository.findByCategoryIdIn(categoryIds);
    }

    private Set<Long> collectDescendantIds(Long rootId, List<AccessoryCategory> allCategories) {
        Set<Long> ids = new HashSet<>();
        ids.add(rootId);
        Map<Long, List<AccessoryCategory>> childrenMap = allCategories.stream()
                .collect(Collectors.groupingBy(AccessoryCategory::getParentId));
        List<Long> frontier = new ArrayList<>();
        frontier.add(rootId);
        while (!frontier.isEmpty()) {
            List<Long> nextFrontier = new ArrayList<>();
            for (Long parentId : frontier) {
                List<AccessoryCategory> children = childrenMap.getOrDefault(parentId, new ArrayList<>());
                for (AccessoryCategory child : children) {
                    if (ids.add(child.getId())) {
                        nextFrontier.add(child.getId());
                    }
                }
            }
            frontier = nextFrontier;
        }
        return ids;
    }

    private List<AccessoryCategory> buildTree(List<AccessoryCategory> allCategories) {
        Map<Long, List<AccessoryCategory>> childrenMap = allCategories.stream()
                .collect(Collectors.groupingBy(AccessoryCategory::getParentId));

        List<AccessoryCategory> roots = childrenMap.getOrDefault(0L, new ArrayList<>());

        for (AccessoryCategory root : roots) {
            setChildren(root, childrenMap);
        }

        return roots;
    }

    private void setChildren(AccessoryCategory parent, Map<Long, List<AccessoryCategory>> childrenMap) {
        List<AccessoryCategory> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (AccessoryCategory child : children) {
            setChildren(child, childrenMap);
        }
    }

    private void clearCache() {
        try {
            stringRedisTemplate.delete(CATEGORY_TREE_KEY);
            log.debug("分类树缓存已清除");
        } catch (Exception e) {
            log.warn("清除分类树缓存失败", e);
        }
    }
}
