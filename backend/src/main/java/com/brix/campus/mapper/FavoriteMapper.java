package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brix.campus.model.Favorite;

/**
 * 收藏 Mapper：取消收藏为物理删除（无逻辑删除列）。
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
