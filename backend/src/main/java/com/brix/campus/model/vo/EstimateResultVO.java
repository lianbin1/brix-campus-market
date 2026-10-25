package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 估价结果出参：LLM 与本地规则返回<b>完全一致的结构</b>，前端零判断（J03）。
 */
@Data
public class EstimateResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 建议价，裁剪到 [1, 99999] */
    private BigDecimal suggestPrice;

    /** 价格区间 */
    private PriceRangeVO priceRange;

    /** 置信度 0~1，降级模式 ≤0.6 */
    private Double confidence;

    /** 定价依据，≥2 条 */
    private List<String> reasons = new ArrayList<>();

    /** 风险提示 */
    private List<String> riskTips = new ArrayList<>();

    /** 数据来源：LLM / RULE */
    private String source;

    /** 今日剩余额度 */
    private Integer remainingQuota;

    /**
     * 价格区间。
     */
    @Data
    public static class PriceRangeVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 区间下限 */
        private BigDecimal min;

        /** 区间上限 */
        private BigDecimal max;

        public PriceRangeVO() {
        }

        public PriceRangeVO(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}
