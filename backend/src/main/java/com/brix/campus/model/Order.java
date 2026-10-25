package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（物理表 {@code t_order}）。
 *
 * <p>{@code trade_code} 为面交 6 位确认码（含前导 0，用 String 承载）；
 * {@code code_fail_count} 必须落库，重启后仍能锁定核销入口（PRD F04）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业务单号：yyyyMMddHHmmss + 6 位随机 */
    @TableField("order_no")
    private String orderNo;

    /** 商品 ID */
    @TableField("item_id")
    private Long itemId;

    /** 买家 ID */
    @TableField("buyer_id")
    private Long buyerId;

    /** 卖家 ID（冗余，省连表判权） */
    @TableField("seller_id")
    private Long sellerId;

    /** 成交金额 */
    private BigDecimal amount;

    /** 商品快照-标题 */
    @TableField("item_title")
    private String itemTitle;

    /** 商品快照-首图 */
    @TableField("item_cover")
    private String itemCover;

    /** 商品快照-单价 */
    @TableField("item_price")
    private BigDecimal itemPrice;

    /** 实际成交方式：MEET / SHIP */
    @TableField("trade_type")
    private String tradeType;

    /** 状态：UNPAID / TO_SHIP / TO_RECEIVE / TO_MEET / DONE / CANCELED */
    private String status;

    /** 支付方式：WECHAT / ALIPAY / BANK */
    @TableField("pay_method")
    private String payMethod;

    /** 支付时间 */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /** 发货时间 */
    @TableField("ship_time")
    private LocalDateTime shipTime;

    /** 完成时间 */
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /** 取消时间 */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /** 关闭原因：TIMEOUT / BUYER_CANCEL / SELLER_CANCEL / SYSTEM */
    @TableField("close_reason")
    private String closeReason;

    /** 面交确认码 6 位（含前导 0） */
    @TableField("trade_code")
    private String tradeCode;

    /** 核销失败次数（持久化，用于 5 次锁定） */
    @TableField("code_fail_count")
    private Integer codeFailCount;

    /** 面交地点 */
    @TableField("meet_place")
    private String meetPlace;

    /** 买家备注 / 邮寄收货信息占位 */
    @TableField("buyer_note")
    private String buyerNote;
}
