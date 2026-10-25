package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评价出参（评价人昵称脱敏，G02）。
 */
@Data
public class ReviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评价 ID */
    private Long id;

    /** 订单 ID */
    private Long orderId;

    /** 商品 ID */
    private Long itemId;

    /** 商品标题 */
    private String itemTitle;

    /** 评分 1~5 */
    private Integer rating;

    /** 文字评价 */
    private String content;

    /** 图片 URL 列表 */
    private List<String> images = new ArrayList<>();

    /** 评价人昵称（脱敏） */
    private String buyerNickname;

    /** 被评卖家 ID */
    private Long sellerId;

    /** 评价时间 */
    private LocalDateTime createdAt;
}
