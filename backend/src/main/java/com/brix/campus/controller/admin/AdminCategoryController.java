package com.brix.campus.controller.admin;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.CategorySaveDTO;
import com.brix.campus.model.vo.CategoryVO;
import com.brix.campus.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台分类管理（K12，仅 OPERATOR）。
 */
@RestController
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 分类列表（含各分类商品数）。
     *
     * @return 分类列表
     */
    @GetMapping
    public R<List<CategoryVO>> list() {
        return R.ok(categoryService.listForAdmin());
    }

    /**
     * 新增分类。
     *
     * @param dto 入参
     * @return 新分类
     */
    @PostMapping
    public R<CategoryVO> create(@RequestBody @Valid CategorySaveDTO dto) {
        return R.ok(categoryService.create(dto), "新增成功");
    }

    /**
     * 编辑分类。
     *
     * @param id  分类 ID
     * @param dto 入参
     * @return 更新后分类
     */
    @PutMapping("/{id}")
    public R<CategoryVO> update(@PathVariable("id") Long id, @RequestBody @Valid CategorySaveDTO dto) {
        return R.ok(categoryService.update(id, dto), "保存成功");
    }

    /**
     * 删除分类（该分类下仍有商品时拒绝）。
     *
     * @param id 分类 ID
     * @return 空
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return R.ok(null, "已删除");
    }
}
