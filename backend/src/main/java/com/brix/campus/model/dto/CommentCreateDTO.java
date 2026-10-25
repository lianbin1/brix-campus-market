package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发表留言 / 回复一楼入参（D02）。
 */
@Data
public class CommentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 内容 1~500 字 */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容不能超过 500 个字")
    private String content;

    /** 父留言 ID：空=一级留言；非空=回复（必须指向同一商品的一级留言） */
    private Long parentId;
}
