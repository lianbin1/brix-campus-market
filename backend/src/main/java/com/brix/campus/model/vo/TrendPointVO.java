package com.brix.campus.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单趋势数据点：连续 7 天，缺日补 0（K03）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 当天订单数 */
    private Long count;
}
