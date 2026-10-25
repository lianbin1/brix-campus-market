package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品查询条件（前台列表 / 我的商品共用）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 关键词：匹配标题或描述 */
    private String keyword;

    /** 分类 ID */
    private Long categoryId;

    /** 校区 */
    private String campus;

    /** 价格下限（含） */
    private BigDecimal minPrice;

    /** 价格上限（含） */
    private BigDecimal maxPrice;

    /** 排序：latest / price_asc / price_desc / hot */
    private String sort;

    /** 状态（「我的商品」使用；前台列表由服务端固定 ON_SALE） */
    private String status;

    /** 卖家 ID（服务端注入，不接受前端传值） */
    private Long sellerId;
}
