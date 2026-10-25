package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体（物理表 {@code t_review}）。{@code order_id} 唯一，保证一单一条（G01）。
 */
@Data
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单 ID，唯一 */
    @TableField("order_id")
    private Long orderId;

    /** 商品 ID（展示用） */
    @TableField("item_id")
    private Long itemId;

    /** 评价人（买家） */
    @TableField("buyer_id")
    private Long buyerId;

    /** 被评人（卖家） */
    @TableField("seller_id")
    private Long sellerId;

    /** 评分 1~5 */
    private Integer rating;

    /** 文字评价 0~500 字 */
    private String content;

    /** 图片 URL 的 JSON 数组，0~3 张 */
    private String images;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
