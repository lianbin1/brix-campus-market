package com.brix.campus.ai;

/**
 * 提示词常量：写死在代码里防止提示漂移（架构 6.2）。
 */
public final class AiPrompts {

    /** 估价系统提示 */
    public static final String ESTIMATE_SYSTEM = """
            你是校园二手交易平台的定价顾问，只服务在校大学生。
            规则：
            1) 必须先调用工具查询真实数据：soldMedianPrice（近 90 天同款成交中位数）、
               onSaleLowestPrice（同款在售最低价）、conditionDiscountFactor（成色折价系数）。
            2) 只输出 JSON，不要输出任何解释文字或 Markdown 代码块。
            3) JSON 字段必须与 EstimateResult 完全一致：
               {"suggestPrice":数字,"priceRange":{"min":数字,"max":数字},
                "confidence":0到1的小数,"reasons":["..."],"riskTips":["..."]}
            4) reasons 至少 2 条，且必须包含具体金额（如「同款 3 笔成交均价 ¥1250」）。
            5) suggestPrice 必须落在 priceRange 区间内；金额单位为元，保留两位小数。
            6) 不提供违规、翻新冒充全新等建议。
            """;

    /** 文案系统提示 */
    public static final String COPY_SYSTEM = """
            你是校园二手交易平台的文案助手。
            规则：
            1) 只输出 JSON，不要输出任何解释文字或 Markdown 代码块。
            2) JSON 字段：{"title":"...","description":"...","tags":["..."]}
            3) title 不超过 40 个字，不含 emoji、不含联系方式。
            4) description 控制在 100~300 字，分三段：成色与瑕疵 / 使用体验 / 交易方式与校区。
            5) tags 给 3~5 个，每个不超过 8 个字。
            6) 不夸大、不虚构成色，不做虚假承诺。
            """;

    private AiPrompts() {
    }

    /**
     * 拼接估价用户提示词。
     *
     * @param profile 归一化入参
     * @return 提示词
     */
    public static String estimateUserPrompt(ItemProfile profile) {
        return "请为下面这件校园二手商品估价：\n"
                + "分类：" + safe(profile.getCategoryName()) + "（ID=" + profile.getCategoryId() + "）\n"
                + "品牌型号：" + safe(profile.getBrandModel()) + "\n"
                + "成色：" + safe(profile.getConditionLevel()) + "\n"
                + "购入月数：" + (profile.getPurchaseMonths() == null ? "未知" : profile.getPurchaseMonths()) + "\n"
                + "原价：" + (profile.getOriginalPrice() == null ? "未知" : profile.getOriginalPrice()) + "\n"
                + "请先调工具查数据，再输出 JSON。";
    }

    /**
     * 拼接文案用户提示词。
     *
     * @param profile 归一化入参
     * @return 提示词
     */
    public static String copyUserPrompt(ItemProfile profile) {
        return "请为下面这件校园二手商品写发布文案：\n"
                + "分类：" + safe(profile.getCategoryName()) + "\n"
                + "品牌型号：" + safe(profile.getBrandModel()) + "\n"
                + "成色：" + safe(profile.getConditionLevel()) + "\n"
                + "购入月数：" + (profile.getPurchaseMonths() == null ? "未知" : profile.getPurchaseMonths()) + "\n"
                + "原价：" + (profile.getOriginalPrice() == null ? "未知" : profile.getOriginalPrice()) + "\n"
                + "请输出 JSON。";
    }

    /**
     * null 安全转字符串。
     *
     * @param value 原值
     * @return 非 null 字符串
     */
    private static String safe(String value) {
        return value == null || value.isBlank() ? "未填写" : value;
    }
}
