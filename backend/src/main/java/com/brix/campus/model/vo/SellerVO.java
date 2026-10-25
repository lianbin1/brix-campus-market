package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 卖家信息卡片（商品详情页使用）。
 */
@Data
public class SellerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卖家 ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 校区 */
    private String campus;

    /** 信用分 */
    private Integer creditScore;
}
