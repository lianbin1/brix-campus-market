package com.brix.campus.model.enums;

/**
 * 用户状态。
 */
public enum UserStatusEnum {

    /** 正常 */
    NORMAL("NORMAL", "正常"),
    /** 已禁用（登录返回 400，已签发 token 的请求返回 403） */
    DISABLED("DISABLED", "已禁用");

    private final String code;
    private final String desc;

    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static UserStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum value : values()) {
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
        UserStatusEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
