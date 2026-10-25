package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息出参。
 */
@Data
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;

    /** 发送者 ID */
    private Long fromUserId;

    /** 接收者 ID */
    private Long toUserId;

    /** 来源商品 ID */
    private Long itemId;

    /** 内容 */
    private String content;

    /** 是否已读：0/1 */
    private Integer isRead;

    /** 发送时间 */
    private LocalDateTime createdAt;
}
