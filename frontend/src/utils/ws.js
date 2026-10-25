import { ElNotification } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

/**
 * 私信 WebSocket 单例（架构 7.2、PRD 7.6）。
 *
 * - 连接地址：ws(s)://<host>/ws/chat?token=<JWT>（dev 由 Vite 代理 /ws → 8080）
 * - 心跳：30 秒一次 PING，服务端回 PONG
 * - 断线：3 秒后自动重连，连不上继续重试
 */

const HEARTBEAT_INTERVAL = 30000
const RECONNECT_DELAY = 3000

let socket = null
let heartbeatTimer = null
let reconnectTimer = null
let manualClosed = false
const listeners = new Set()

/** 计算 WebSocket 地址 */
function buildUrl() {
  const auth = useAuthStore()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws/chat?token=${encodeURIComponent(auth.token)}`
}

/** 清理定时器 */
function clearTimers() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

/** 分发收到的事件给所有订阅者 */
function dispatch(payload) {
  listeners.forEach((handler) => {
    try {
      handler(payload)
    } catch (error) {
      console.warn('[ws] 消息处理异常', error)
    }
  })
}

/** 建立连接 */
function connect() {
  const auth = useAuthStore()
  if (!auth.token) return
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return

  manualClosed = false
  socket = new WebSocket(buildUrl())

  socket.onopen = () => {
    dispatch({ type: 'OPEN' })
    heartbeatTimer = setInterval(() => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'PING' }))
      }
    }, HEARTBEAT_INTERVAL)
  }

  socket.onmessage = (event) => {
    let payload
    try {
      payload = JSON.parse(event.data)
    } catch (error) {
      return
    }
    if (payload.type === 'PONG') return
    dispatch(payload)
  }

  socket.onclose = () => {
    dispatch({ type: 'CLOSE' })
    clearTimers()
    if (!manualClosed) {
      reconnectTimer = setTimeout(() => connect(), RECONNECT_DELAY)
    }
  }

  socket.onerror = () => {
    // 由 onclose 统一处理重连
  }
}

/** 主动断开（登出时调用） */
function disconnect() {
  manualClosed = true
  clearTimers()
  if (socket) {
    socket.close()
    socket = null
  }
}

/**
 * 订阅消息。
 * @param {(payload: Record<string, unknown>) => void} handler 处理函数
 * @returns {() => void} 取消订阅
 */
function onMessage(handler) {
  listeners.add(handler)
  return () => listeners.delete(handler)
}

/**
 * 发送私信。
 * @param {{toUserId: string, itemId?: string|null, content: string}} payload 消息体
 * @param {string} [clientMsgId] 本地临时消息 id（服务端回执原样回填，用于替换乐观气泡）
 * @returns {boolean} 是否已发出（未连上则返回 false，调用方可回落到 HTTP 接口）
 */
function sendChat(payload, clientMsgId) {
  if (!socket || socket.readyState !== WebSocket.OPEN) return false
  socket.send(
    JSON.stringify({
      type: 'CHAT',
      toUserId: payload.toUserId,
      itemId: payload.itemId ?? null,
      content: payload.content,
      // 端到端去重：回执帧带回同一个值，前端据此把 tmp 气泡换成真实气泡（BUG-A 修复）
      clientMsgId: clientMsgId ?? null
    })
  )
  return true
}

/** 是否已连接 */
function isOpen() {
  return !!socket && socket.readyState === WebSocket.OPEN
}

/**
 * 收到非当前会话的新消息时给出桌面提示。
 * @param {string} content 消息内容
 */
function notifyNewMessage(content) {
  ElNotification({
    title: '收到一条新私信',
    message: content,
    type: 'info',
    duration: 3000
  })
}

export default {
  connect,
  disconnect,
  onMessage,
  sendChat,
  isOpen,
  notifyNewMessage
}
