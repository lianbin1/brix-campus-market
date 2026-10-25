package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 私信发送入参（I01）：HTTP 兜底通道与 WebSocket 共用同一 Service 方法。
 */
@Data
public class ChatSendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 接收者 ID */
    @NotNull(message = "接收人不能为空")
    private Long toUserId;

    /** 来源商品 ID，可空 */
    private Long itemId;

    /** 内容 1~500 字 */
    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "消息内容不能超过 500 个字")
    private String content;
}
