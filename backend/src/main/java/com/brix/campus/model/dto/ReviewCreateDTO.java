package com.brix.campus.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交评价入参（G01）：评分 1~5，文字 0~500 字，图片 0~3 张。
 */
@Data
public class ReviewCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评分 1~5 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须为 1-5")
    @Max(value = 5, message = "评分必须为 1-5")
    private Integer rating;

    /** 文字评价 0~500 字 */
    @Size(max = 500, message = "评价内容不能超过 500 个字")
    private String content;

    /** 图片 URL 列表，0~3 张 */
    private List<String> images;
}
