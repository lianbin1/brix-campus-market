package com.brix.campus.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 智能估价入参（J01）。
 */
@Data
public class AiEstimateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类 ID */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /** 品牌型号，如「iPad 9 64G」 */
    @NotBlank(message = "品牌型号不能为空")
    @Size(max = 50, message = "品牌型号不能超过 50 个字")
    private String brandModel;

    /** 成色：BRAND_NEW / LIKE_NEW / LIGHT_USE / HEAVY_USE */
    @NotBlank(message = "成色不能为空")
    private String conditionLevel;

    /** 购入月数，可空 */
    @Min(value = 0, message = "购入时间不能为负数")
    private Integer purchaseMonths;

    /** 原价，可空 */
    private BigDecimal originalPrice;
}
