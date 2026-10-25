package com.brix.campus.model.enums;

/**
 * 模拟支付方式（不接真实支付 SDK）。
 */
public enum PayMethodEnum {

    /** 微信支付 */
    WECHAT("WECHAT", "微信支付"),
    /** 支付宝 */
    ALIPAY("ALIPAY", "支付宝"),
    /** 银行卡 */
    BANK("BANK", "银行卡");

    private final String code;
    private final String desc;

    PayMethodEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PayMethodEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (PayMethodEnum value : values()) {
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
        PayMethodEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
