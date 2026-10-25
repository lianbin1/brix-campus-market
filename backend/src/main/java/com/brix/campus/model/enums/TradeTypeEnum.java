package com.brix.campus.model.enums;

/**
 * 交易方式。商品可标 {@code BOTH}，下单时由买家二选一，订单上只记 {@code MEET}/{@code SHIP}。
 */
public enum TradeTypeEnum {

    /** 面交 */
    MEET("MEET", "面交"),
    /** 邮寄 */
    SHIP("SHIP", "邮寄"),
    /** 都可 */
    BOTH("BOTH", "面交或邮寄");

    private final String code;
    private final String desc;

    TradeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TradeTypeEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (TradeTypeEnum value : values()) {
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
        TradeTypeEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
