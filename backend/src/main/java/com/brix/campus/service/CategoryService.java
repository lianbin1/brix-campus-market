package com.brix.campus.service;

import com.brix.campus.model.Category;
import com.brix.campus.model.dto.CategorySaveDTO;
import com.brix.campus.model.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务。
 */
public interface CategoryService {

    /**
     * 前台分类列表（按 sort 升序）。
     *
     * @return 分类列表
     */
    List<CategoryVO> listForFront();

    /**
     * 后台分类列表（含各分类商品数）。
     *
     * @return 分类列表
     */
    List<CategoryVO> listForAdmin();

    /**
     * 新增分类。
     *
     * @param dto 入参
     * @return 新建分类
     */
    CategoryVO create(CategorySaveDTO dto);

    /**
     * 编辑分类。
     *
     * @param id  分类 ID
     * @param dto 入参
     * @return 更新后分类
     */
    CategoryVO update(Long id, CategorySaveDTO dto);

    /**
     * 删除分类（该分类下仍有商品时拒绝）。
     *
     * @param id 分类 ID
     */
    void delete(Long id);

    /**
     * 取分类实体。
     *
     * @param id 分类 ID
     * @return 分类，不存在返回 null
     */
    Category getEntity(Long id);

    /**
     * 取分类名，不存在返回空串。
     *
     * @param id 分类 ID
     * @return 分类名
     */
    String nameOf(Long id);
}
