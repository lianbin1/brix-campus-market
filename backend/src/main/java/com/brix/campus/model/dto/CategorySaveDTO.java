package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增 / 编辑入参（K12）。
 */
@Data
public class CategorySaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类名称，唯一 */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 30, message = "分类名称不能超过 30 个字")
    private String name;

    /** 图标名 */
    @Size(max = 50, message = "图标名过长")
    private String icon;

    /** 排序值，升序展示 */
    private Integer sort;
}
