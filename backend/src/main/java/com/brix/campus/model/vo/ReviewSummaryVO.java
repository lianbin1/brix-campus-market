package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 卖家评价汇总：平均分 + 总数（无评价时 avgRating=0、total=0，不返回 404）。
 */
@Data
public class ReviewSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 平均分（保留一位小数） */
    private BigDecimal avgRating = BigDecimal.ZERO;

    /** 评价总数 */
    private Long total = 0L;
}
