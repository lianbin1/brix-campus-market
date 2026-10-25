package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.vo.CategoryVO;
import com.brix.campus.service.CategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开分类接口（B09，无需登录）。
 */
@RestController
@RequestMapping("/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 全部分类，按 sort 升序。
     *
     * @return 分类列表
     */
    @GetMapping
    public R<List<CategoryVO>> list() {
        return R.ok(categoryService.listForFront());
    }
}
