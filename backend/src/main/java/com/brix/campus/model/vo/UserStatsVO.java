package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人数据统计（个人中心页面 11 使用）。
 */
@Data
public class UserStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 发布总数 */
    private Long itemCount = 0L;

    /** 在售数 */
    private Long onSaleCount = 0L;

    /** 卖出数（已完成订单数） */
    private Long soldCount = 0L;
}
