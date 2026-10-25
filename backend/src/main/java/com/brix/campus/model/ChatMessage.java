package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信实体（物理表 {@code t_chat_message}）。不建会话表，会话按「对方用户 ID」聚合。
 */
@Data
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键兼消息序号（历史按 id 升序） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者 */
    @TableField("from_user_id")
    private Long fromUserId;

    /** 接收者 */
    @TableField("to_user_id")
    private Long toUserId;

    /** 来源商品，可空 */
    @TableField("item_id")
    private Long itemId;

    /** 内容 1~500 字 */
    private String content;

    /** 是否已读：0 未读 / 1 已读（打开会话统一置 1） */
    @TableField("is_read")
    private Integer isRead;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
