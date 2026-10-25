package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的收藏列表项（已售出/已下架仍展示，需带状态角标）。
 */
@Data
public class FavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收藏记录 ID */
    private Long id;

    /** 商品 ID */
    private Long itemId;

    /** 商品标题 */
    private String itemTitle;

    /** 商品首图 */
    private String itemCover;

    /** 商品价格 */
    private BigDecimal itemPrice;

    /** 商品状态码 */
    private String itemStatus;

    /** 商品状态文案 */
    private String itemStatusDesc;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}
