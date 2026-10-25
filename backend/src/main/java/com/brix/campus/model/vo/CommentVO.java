package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 留言出参：一级留言下挂 {@code replies}（按时间升序，仅一层）。
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 留言 ID */
    private Long id;

    /** 商品 ID */
    private Long itemId;

    /** 留言人 ID */
    private Long userId;

    /** 留言人昵称 */
    private String userNickname;

    /** 留言人头像 */
    private String userAvatar;

    /** 内容 */
    private String content;

    /** 父留言 ID，空=一级留言 */
    private Long parentId;

    /** 留言时间 */
    private LocalDateTime createdAt;

    /** 当前登录用户是否为作者（前端据此显示删除按钮） */
    private Boolean canDelete;

    /** 回复列表 */
    private List<CommentVO> replies = new ArrayList<>();
}
