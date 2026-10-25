import request from '@/utils/request'

/**
 * 私信接口（I01-I04）。WebSocket 用于实时收发，这里提供 HTTP 兜底与历史查询。
 */

/** 会话列表（按对方聚合，时间倒序） */
export function fetchConversations() {
  return request.get('/chat/conversations')
}

/** 历史消息（双向，按 id 升序） */
export function fetchMessages(params) {
  return request.get('/chat/messages', { params })
}

/** 全局未读数 */
export function fetchUnreadCount() {
  return request.get('/chat/unread-count')
}

/** 打开会话即已读，返回清零后的全局未读数 */
export function markRead(peerId) {
  return request.put('/chat/read', null, { params: { peerId } })
}

/** 发送消息（HTTP 兜底通道，与 WS 同一服务方法） */
export function sendMessage(data) {
  return request.post('/chat/messages', data)
}
