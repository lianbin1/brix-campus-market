package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据大屏核心指标（K02）。
 */
@Data
public class DashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 注册用户数 */
    private Long userCount = 0L;

    /** 在售商品数 */
    private Long onSaleCount = 0L;

    /** 今日订单数 */
    private Long todayOrderCount = 0L;

    /** 累计成交额（仅统计 TO_SHIP / TO_RECEIVE / TO_MEET / DONE） */
    private BigDecimal totalTradeAmount = BigDecimal.ZERO;
}
