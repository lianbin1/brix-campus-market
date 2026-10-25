package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体（物理表 {@code t_favorite}）。
 *
 * <p>取消收藏用<b>物理删除</b>，否则 {@code uk_user_item} 唯一索引会挡住再次收藏（架构 3.6）。</p>
 */
@Data
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏人 */
    @TableField("user_id")
    private Long userId;

    /** 商品 */
    @TableField("item_id")
    private Long itemId;

    /** 收藏时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
