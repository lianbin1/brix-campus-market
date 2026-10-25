package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品图片实体（物理表 {@code t_item_image}）。
 *
 * <p>无逻辑删除列：编辑商品时整组物理替换（架构 3.5）。</p>
 */
@Data
public class ItemImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品 ID */
    @TableField("item_id")
    private Long itemId;

    /** 图片 URL：/uploads/item/202610/xxx.jpg */
    private String url;

    /** 排序，0 为首图，最多 9 张 */
    private Integer sort;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
