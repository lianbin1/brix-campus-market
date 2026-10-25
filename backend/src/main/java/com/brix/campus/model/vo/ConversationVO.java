package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信会话列表项：按「对方用户 ID」聚合，不建会话表（I02）。
 */
@Data
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 对方用户 ID */
    private Long peerId;

    /** 对方昵称 */
    private String peerNickname;

    /** 对方头像 */
    private String peerAvatar;

    /** 最后一条消息 ID */
    private Long lastMessageId;

    /** 最后一条消息内容 */
    private String lastContent;

    /** 最后一条消息时间 */
    private LocalDateTime lastTime;

    /** 该会话未读数 */
    private Long unreadCount;
}
