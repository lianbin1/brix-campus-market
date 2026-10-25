package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.CategoryMapper;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.model.Category;
import com.brix.campus.model.Item;
import com.brix.campus.model.dto.CategorySaveDTO;
import com.brix.campus.model.vo.CategoryVO;
import com.brix.campus.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现。
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ItemMapper itemMapper;

    public CategoryServiceImpl(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public List<CategoryVO> listForFront() {
        List<Category> categories = list(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        List<CategoryVO> result = new ArrayList<>(categories.size());
        for (Category category : categories) {
            result.add(toVO(category, null));
        }
        return result;
    }

    @Override
    public List<CategoryVO> listForAdmin() {
        List<Category> categories = list(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        Map<Long, Long> countMap = countItemByCategory();
        List<CategoryVO> result = new ArrayList<>(categories.size());
        for (Category category : categories) {
            result.add(toVO(category, countMap.getOrDefault(category.getId(), 0L)));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategorySaveDTO dto) {
        String name = dto.getName().trim();
        assertNameAvailable(name, null);
        Category category = new Category();
        category.setName(name);
        category.setIcon(dto.getIcon() == null ? "" : dto.getIcon().trim());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        save(category);
        return toVO(category, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO update(Long id, CategorySaveDTO dto) {
        Category category = getEntity(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        String name = dto.getName().trim();
        assertNameAvailable(name, id);
        category.setName(name);
        category.setIcon(dto.getIcon() == null ? "" : dto.getIcon().trim());
        category.setSort(dto.getSort() == null ? category.getSort() : dto.getSort());
        updateById(category);
        return toVO(category, countItemByCategory().getOrDefault(id, 0L));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category category = getEntity(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        Long itemCount = itemMapper.selectCount(Wrappers.<Item>lambdaQuery()
                .eq(Item::getCategoryId, id));
        if (itemCount != null && itemCount > 0) {
            throw BusinessException.param("该分类下仍有商品");
        }
        removeById(id);
    }

    @Override
    public Category getEntity(Long id) {
        if (id == null) {
            return null;
        }
        return getById(id);
    }

    @Override
    public String nameOf(Long id) {
        Category category = getEntity(id);
        return category == null ? "" : category.getName();
    }

    /**
     * 校验分类名不重复。
     *
     * @param name      分类名
     * @param excludeId 编辑时排除自身 ID
     */
    private void assertNameAvailable(String name, Long excludeId) {
        Long count = baseMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getName, name)
                .ne(excludeId != null, Category::getId, excludeId));
        if (count != null && count > 0) {
            throw BusinessException.param("分类名称已存在");
        }
    }

    /**
     * 统计各分类下的商品数量（全状态，删除态商品不计）。
     *
     * @return 分类 ID → 商品数
     */
    private Map<Long, Long> countItemByCategory() {
        Map<Long, Long> result = new HashMap<>();
        List<Map<String, Object>> rows = itemMapper.countItemGroupByCategory();
        for (Map<String, Object> row : rows) {
            Object idValue = row.get("categoryId");
            Object countValue = row.get("cnt");
            if (idValue instanceof Number idNumber && countValue instanceof Number countNumber) {
                result.put(idNumber.longValue(), countNumber.longValue());
            }
        }
        return result;
    }

    /**
     * 实体转 VO。
     *
     * @param category 分类实体
     * @param itemCount 商品数（可为 null）
     * @return VO
     */
    private CategoryVO toVO(Category category, Long itemCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSort());
        vo.setItemCount(itemCount);
        return vo;
    }
}
