package com.brix.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.model.query.BaseQuery;
import com.brix.campus.model.vo.FavoriteVO;

import java.util.Map;

/**
 * 收藏服务（C01、C02）。
 */
public interface FavoriteService {

    /**
     * 收藏 / 取消收藏（切换式）。
     *
     * @param userId 用户 ID
     * @param itemId 商品 ID
     * @return {favorited:boolean, favoriteCount:int}
     */
    Map<String, Object> toggle(Long userId, Long itemId);

    /**
     * 我的收藏分页（已售出 / 已下架商品仍展示并带状态）。
     *
     * @param userId 用户 ID
     * @param query  分页条件
     * @return 分页结果
     */
    IPage<FavoriteVO> page(Long userId, BaseQuery query);
}
