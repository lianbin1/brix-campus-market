package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台全平台订单列表项（K11）。
 */
@Data
public class AdminOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单 ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 商品 ID */
    private Long itemId;

    /** 商品标题 */
    private String itemTitle;

    /** 商品首图 */
    private String itemCover;

    /** 成交金额 */
    private BigDecimal amount;

    /** 买家 ID */
    private Long buyerId;

    /** 买家用户名 */
    private String buyerUsername;

    /** 买家昵称 */
    private String buyerNickname;

    /** 卖家 ID */
    private Long sellerId;

    /** 卖家用户名 */
    private String sellerUsername;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 支付方式码 */
    private String payMethod;

    /** 支付方式文案 */
    private String payMethodDesc;

    /** 状态码 */
    private String status;

    /** 状态文案 */
    private String statusDesc;

    /** 交易方式码 */
    private String tradeType;

    /** 下单时间 */
    private LocalDateTime createdAt;

    /** 支付时间 */
    private LocalDateTime payTime;
}
