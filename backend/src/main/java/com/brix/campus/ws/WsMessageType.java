package com.brix.campus.ws;

/**
 * WebSocket 消息类型。
 */
public enum WsMessageType {

    /** 文本私信 */
    CHAT,
    /** 心跳请求 */
    PING,
    /** 心跳响应 */
    PONG,
    /** 已读通知 */
    READ,
    /** 错误帧（不断连，便于前端改后重发） */
    ERROR
}
