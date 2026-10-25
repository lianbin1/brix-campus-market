package com.brix.campus.model.enums;

/**
 * 举报处理状态。
 */
public enum ReportStatusEnum {

    /** 待处理 */
    PENDING("PENDING", "待处理"),
    /** 已处理 */
    HANDLED("HANDLED", "已处理");

    private final String code;
    private final String desc;

    ReportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReportStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ReportStatusEnum value : values()) {
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
        ReportStatusEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
