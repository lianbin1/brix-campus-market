package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台全平台私信检索列表项（K13，只读，默认按发送时间倒序）。
 *
 * <p>消息不可撤回、不可删除，仅提供查看能力，避免交易纠纷无据可查。</p>
 */
@Data
public class AdminChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;

    /** 消息内容 */
    private String content;

    /** 发送方 */
    private ChatUserVO fromUser;

    /** 接收方 */
    private ChatUserVO toUser;

    /** 关联商品 ID，无则为 null */
    private Long itemId;

    /** 关联商品标题，无则为 null */
    private String itemTitle;

    /** 发送时间 */
    private LocalDateTime createdAt;

    /** 是否已读：0 未读 / 1 已读 */
    private Integer isRead;
}
