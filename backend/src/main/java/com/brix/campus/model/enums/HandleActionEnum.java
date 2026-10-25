package com.brix.campus.model.enums;

/**
 * 举报处理动作。
 */
public enum HandleActionEnum {

    /** 下架商品 */
    OFFLINE("OFFLINE", "下架商品"),
    /** 驳回举报 */
    REJECT("REJECT", "驳回举报");

    private final String code;
    private final String desc;

    HandleActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static HandleActionEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (HandleActionEnum value : values()) {
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
        HandleActionEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
