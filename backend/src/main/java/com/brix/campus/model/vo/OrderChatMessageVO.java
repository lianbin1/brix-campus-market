package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单维度私信记录中的单条消息（后台只读，按 {@code created_at} 升序）。
 */
@Data
public class OrderChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;

    /** 发送者 ID */
    private Long fromUserId;

    /** 发送者昵称 */
    private String fromNickname;

    /** 接收者 ID */
    private Long toUserId;

    /** 消息内容（不可撤回 / 不可删除） */
    private String content;

    /** 发送时间 */
    private LocalDateTime createdAt;
}
