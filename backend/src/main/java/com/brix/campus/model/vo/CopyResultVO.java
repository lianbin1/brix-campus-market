package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 文案生成出参：标题 ≤40 字、描述 100~300 字、标签 3~5 个。
 */
@Data
public class CopyResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 标签 */
    private List<String> tags = new ArrayList<>();

    /** 数据来源：LLM / RULE */
    private String source;

    /** 今日剩余额度 */
    private Integer remainingQuota;
}
