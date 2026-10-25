package com.brix.campus.model.enums;

/**
 * AI 调用场景（估价与文案共用同一 20 次/天计数池）。
 */
public enum AiSceneEnum {

    /** 智能估价 */
    ESTIMATE("ESTIMATE", "智能估价"),
    /** 一键生成标题与描述 */
    COPY("COPY", "文案生成");

    private final String code;
    private final String desc;

    AiSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AiSceneEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (AiSceneEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String descOf(String code) {
        AiSceneEnum value = of(code);
        return value == null ? code : value.desc;
    }
}
