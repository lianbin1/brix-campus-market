package com.brix.campus.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品图片出参。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemImageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片 ID */
    private Long id;

    /** 图片 URL */
    private String url;

    /** 排序，0 为首图 */
    private Integer sort;
}
