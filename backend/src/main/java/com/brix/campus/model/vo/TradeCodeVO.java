package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 面交确认码出参（F02 查看 / F03 核销共用）。
 */
@Data
public class TradeCodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 6 位确认码（仅买卖双方可见） */
    private String tradeCode;

    /** 累计失败次数 */
    private Integer failCount;

    /** 剩余可试次数 */
    private Integer remainingAttempts;

    /** 是否已锁定 */
    private Boolean locked;

    /** 核销是否成功（仅核销接口返回） */
    private Boolean success;

    /** 核销后订单状态码 */
    private String orderStatus;

    /** 核销后订单状态文案 */
    private String orderStatusDesc;
}
