package com.brix.campus.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分类分布饼图数据项：各分类在售商品数（K04）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类 ID */
    private Long categoryId;

    /** 分类名称 */
    private String name;

    /** 在售商品数 */
    private Long value;
}
