package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台商品列表项（审核列表 / 待审核 Top10）。
 */
@Data
public class AdminItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 首图 */
    private String coverImage;

    /** 价格 */
    private BigDecimal price;

    /** 分类 ID */
    private Long categoryId;

    /** 分类名 */
    private String categoryName;

    /** 卖家 ID */
    private Long sellerId;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 校区 */
    private String campus;

    /** 状态码 */
    private String status;

    /** 状态文案 */
    private String statusDesc;

    /** 拒绝原因 */
    private String rejectReason;

    /** 提交时间 */
    private LocalDateTime createdAt;
}
