package com.brix.campus.model.enums;

/**
 * 商品状态（仅 5 种，见 PRD B08 / 商品状态机）。
 */
public enum ItemStatusEnum {

    /** 待审核 */
    PENDING("PENDING", "待审核"),
    /** 在售 */
    ON_SALE("ON_SALE", "在售"),
    /** 已下架 */
    OFF_SHELF("OFF_SHELF", "已下架"),
    /** 已售出 */
    SOLD("SOLD", "已售出"),
    /** 审核拒绝 */
    REJECTED("REJECTED", "审核拒绝");

    private final String code;
    private final String desc;

    ItemStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ItemStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ItemStatusEnum value : values()) {
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
        ItemStatusEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
