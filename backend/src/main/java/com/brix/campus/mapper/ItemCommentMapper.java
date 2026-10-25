package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brix.campus.model.ItemComment;

/**
 * 留言 Mapper：一级留言与回复同表，靠 parent_id 区分。
 */
public interface ItemCommentMapper extends BaseMapper<ItemComment> {
}
