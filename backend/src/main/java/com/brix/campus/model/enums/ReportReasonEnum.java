package com.brix.campus.model.enums;

/**
 * 举报原因（枚举，非法值返回 400「举报原因不合法」）。
 */
public enum ReportReasonEnum {

    /** 虚假信息 */
    FAKE_INFO("FAKE_INFO", "虚假信息"),
    /** 违禁物品 */
    CONTRABAND("CONTRABAND", "违禁物品"),
    /** 价格欺诈 */
    PRICE_FRAUD("PRICE_FRAUD", "价格欺诈"),
    /** 盗图 */
    STOLEN_IMAGE("STOLEN_IMAGE", "盗图"),
    /** 其他 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    ReportReasonEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReportReasonEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ReportReasonEnum value : values()) {
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
        ReportReasonEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
