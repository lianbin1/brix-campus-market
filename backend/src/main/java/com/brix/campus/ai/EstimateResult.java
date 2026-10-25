package com.brix.campus.ai;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 估价结果：<b>LLM 与本地规则共用同一结构</b>，前端零判断（J01、J03）。
 */
@Data
public class EstimateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 建议价 */
    private BigDecimal suggestPrice;

    /** 价格区间 */
    private PriceRange priceRange;

    /** 置信度 0~1 */
    private double confidence;

    /** 定价依据（≥2 条） */
    private List<String> reasons = new ArrayList<>();

    /** 风险提示 */
    private List<String> riskTips = new ArrayList<>();

    /**
     * 价格区间 {min, max}。
     */
    @Data
    public static class PriceRange implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 下限 */
        private BigDecimal min;

        /** 上限 */
        private BigDecimal max;

        public PriceRange() {
        }

        public PriceRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}
