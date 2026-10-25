package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单查询条件（买家 / 卖家 / 后台共用）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 状态筛选，空=全部 */
    private String status;

    /** 后台关键词：订单号 / 商品标题 / 买卖家用户名 */
    private String keyword;

    /** 买家 ID（服务端注入） */
    private Long buyerId;

    /** 卖家 ID（服务端注入） */
    private Long sellerId;
}
