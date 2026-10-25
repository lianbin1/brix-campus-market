package com.brix.campus.model.enums;

/**
 * 成色枚举（折价系数用于 AI 规则估价）。
 */
public enum ConditionEnum {

    /** 全新 */
    BRAND_NEW("BRAND_NEW", "全新", 1.00d),
    /** 几乎全新 */
    LIKE_NEW("LIKE_NEW", "几乎全新", 0.85d),
    /** 轻微使用 */
    LIGHT_USE("LIGHT_USE", "轻微使用", 0.70d),
    /** 明显使用 */
    HEAVY_USE("HEAVY_USE", "明显使用", 0.55d);

    private final String code;
    private final String desc;
    private final double factor;

    ConditionEnum(String code, String desc, double factor) {
        this.code = code;
        this.desc = desc;
        this.factor = factor;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 成色折价系数（PRD 7.6） */
    public double getFactor() {
        return factor;
    }

    public static ConditionEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ConditionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return of(code) != null;
    }

    public static String descOf(String code) {
        ConditionEnum value = of(code);
        return value == null ? code : value.desc;
    }

    /** 成色系数，非法入参按「轻微使用」兜底 */
    public static double factorOf(String code) {
        ConditionEnum value = of(code);
        return value == null ? LIGHT_USE.factor : value.factor;
    }
}
