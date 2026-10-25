package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 留言查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    private Long itemId;

    /** 卖家 ID（评价列表使用） */
    private Long sellerId;
}
