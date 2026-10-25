package com.brix.campus.ws;

import lombok.Data;

import java.io.Serializable;

/**
 * WebSocket 帧结构（服务端与前端约定的唯一协议，架构 7.4）。
 *
 * <p>所有 ID 以字符串传输，与服务端「Long → String」序列化策略一致。</p>
 */
@Data
public class WsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 帧类型：CHAT / PING / PONG / READ / ERROR */
    private String type;

    /** 发送者 ID（服务端按 token 填充，忽略客户端传值） */
    private String fromUserId;

    /** 接收者 ID */
    private String toUserId;

    /** 来源商品 ID，可为空 */
    private String itemId;

    /** 文本内容 */
    private String content;

    /** 服务端生成的时间 yyyy-MM-dd HH:mm:ss */
    private String timestamp;

    /** 落库后的消息 ID */
    private String messageId;

    /**
     * 客户端本地消息 ID（发送方生成的乐观气泡 id）。
     *
     * <p>作用：发送方发出 CHAT 帧时携带，服务端在回执（ack）帧里<b>原样回填</b>，
     * 前端据此把本地临时气泡替换为落库后的真实气泡，避免「发一条显示两条」（BUG-A）。
     * 推送给接收方的 CHAT 帧该字段保持 {@code null}。</p>
     */
    private String clientMsgId;
}
