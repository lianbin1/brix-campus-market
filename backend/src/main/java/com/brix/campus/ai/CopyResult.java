package com.brix.campus.ai;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文案生成结果：标题 ≤40 字、描述 100~300 字、标签 3~5 个（J02）。
 */
@Data
public class CopyResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 标签 */
    private List<String> tags = new ArrayList<>();
}
