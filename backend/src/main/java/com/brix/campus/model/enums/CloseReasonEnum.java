package com.brix.campus.model.enums;

/**
 * 订单关闭原因。
 */
public enum CloseReasonEnum {

    /** 超时未支付（定时任务） */
    TIMEOUT("TIMEOUT", "超时未支付"),
    /** 买家取消 */
    BUYER_CANCEL("BUYER_CANCEL", "买家取消"),
    /** 卖家取消 */
    SELLER_CANCEL("SELLER_CANCEL", "卖家取消"),
    /** 系统关闭（同商品其它订单被抢占） */
    SYSTEM("SYSTEM", "系统关闭");

    private final String code;
    private final String desc;

    CloseReasonEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CloseReasonEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (CloseReasonEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String descOf(String code) {
        CloseReasonEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
