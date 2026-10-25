package com.brix.campus.model.enums;

/**
 * 用户角色。
 */
public enum RoleEnum {

    /** 普通用户（学生，既可买也可卖） */
    USER("USER", "普通用户"),
    /** 审核员（商品审核 / 强制下架 / 举报处理） */
    REVIEWER("REVIEWER", "审核员"),
    /** 运营（数据大屏 / 用户管理 / 订单查询 / 分类管理） */
    OPERATOR("OPERATOR", "运营");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RoleEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum value : values()) {
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
        RoleEnum value = of(code);
        return value == null ? code : value.desc;
    }

    /** 是否后台角色（审核员或运营） */
    public static boolean isAdmin(String code) {
        return REVIEWER.code.equals(code) || OPERATOR.code.equals(code);
    }
}
