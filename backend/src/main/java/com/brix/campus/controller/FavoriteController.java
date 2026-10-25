package com.brix.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.query.BaseQuery;
import com.brix.campus.model.vo.FavoriteVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 收藏接口（C01、C02）。
 *
 * <p>取消收藏复用切换式接口 {@code POST /items/{id}/favorite}，不单设 DELETE。</p>
 */
@RestController
@Validated
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 收藏 / 取消收藏（切换式）。
     *
     * @param id 商品 ID
     * @return {favorited, favoriteCount}
     */
    @PostMapping("/items/{id}/favorite")
    public R<Map<String, Object>> toggle(@PathVariable("id") Long id) {
        return R.ok(favoriteService.toggle(SecurityUtils.currentUserId(), id));
    }

    /**
     * 我的收藏分页。
     *
     * @param query 分页条件
     * @return 分页结果
     */
    @GetMapping("/favorites")
    public R<IPage<FavoriteVO>> page(@Valid BaseQuery query) {
        return R.ok(favoriteService.page(SecurityUtils.currentUserId(), query));
    }
}
