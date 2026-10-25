package com.brix.campus.ws;

import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ChatSendDTO;
import com.brix.campus.model.enums.UserStatusEnum;
import com.brix.campus.model.vo.ChatMessageVO;
import com.brix.campus.security.JwtUtil;
import com.brix.campus.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 私信 WebSocket 端点：{@code ws://host/ws/chat?token=<JWT>}。
 *
 * <p>为什么用 {@code @ServerEndpoint}：握手取参只需读 query string，实现量最小（架构 7.1）。</p>
 *
 * <p>依赖注入说明：容器会为每个连接 new 一个端点实例，Spring 不会给它注依赖，
 * 因此这里用 <b>静态字段 + Spring setter 注入</b> 的方式共享单例 Bean。</p>
 */
@ServerEndpoint("/ws/chat")
@Component
public class ChatWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** session 属性键：当前用户 ID */
    public static final String ATTR_USER_ID = "userId";
    /** 帧时间格式 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 连接 → 用户 ID 映射，避免关闭时重复解析 */
    private static final Map<String, Long> SESSION_USERS = new ConcurrentHashMap<>();

    private static JwtUtil jwtUtil;
    private static UserMapper userMapper;
    private static WsSessionManager wsSessionManager;
    private static ChatService chatService;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setJwtUtil(JwtUtil value) {
        ChatWebSocketHandler.jwtUtil = value;
    }

    @Autowired
    public void setUserMapper(UserMapper value) {
        ChatWebSocketHandler.userMapper = value;
    }

    @Autowired
    public void setWsSessionManager(WsSessionManager value) {
        ChatWebSocketHandler.wsSessionManager = value;
    }

    @Autowired
    public void setChatService(ChatService value) {
        ChatWebSocketHandler.chatService = value;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper value) {
        ChatWebSocketHandler.objectMapper = value;
    }

    /**
     * 握手：校验 token 与账号状态，失败立即关闭连接（PRD I01）。
     *
     * @param session 会话
     * @param config  端点配置
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            closeQuietly(session, "unauthorized");
            return;
        }
        User user = userMapper == null ? null : userMapper.selectById(userId);
        if (user == null || UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            closeQuietly(session, "forbidden");
            return;
        }
        session.getUserProperties().put(ATTR_USER_ID, userId);
        SESSION_USERS.put(session.getId(), userId);
        wsSessionManager.add(userId, session);
        log.debug("WebSocket 连接建立：userId={}, sessionId={}", userId, session.getId());
    }

    /**
     * 接收帧：CHAT 落库并推送；PING 回 PONG；非法帧回 ERROR 但不断连。
     *
     * @param text    帧文本
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String text, Session session) {
        Long userId = SESSION_USERS.get(session.getId());
        if (userId == null) {
            sendError(session, userId, "连接未认证");
            return;
        }
        WsMessage frame;
        try {
            frame = objectMapper.readValue(text, WsMessage.class);
        } catch (Exception e) {
            sendError(session, userId, "消息格式错误");
            return;
        }
        String type = frame == null || frame.getType() == null ? "" : frame.getType();
        if (WsMessageType.PING.name().equals(type)) {
            WsMessage pong = new WsMessage();
            pong.setType(WsMessageType.PONG.name());
            pong.setTimestamp(LocalDateTime.now().format(TIME_FORMATTER));
            wsSessionManager.sendToSession(userId, session, pong);
            return;
        }
        if (!WsMessageType.CHAT.name().equals(type)) {
            sendError(session, userId, "不支持的消息类型");
            return;
        }

        try {
            ChatSendDTO dto = new ChatSendDTO();
            dto.setToUserId(parseLong(frame.getToUserId()));
            dto.setItemId(parseLong(frame.getItemId()));
            dto.setContent(frame.getContent());
            ChatMessageVO message = chatService.send(userId, dto);

            // 回执给发送方（多标签页同步 / 失败重试判断）
            WsMessage ack = new WsMessage();
            ack.setType(WsMessageType.CHAT.name());
            ack.setFromUserId(String.valueOf(userId));
            ack.setToUserId(frame.getToUserId());
            ack.setItemId(frame.getItemId());
            ack.setContent(message.getContent());
            ack.setMessageId(String.valueOf(message.getId()));
            ack.setTimestamp(message.getCreatedAt() == null
                    ? LocalDateTime.now().format(TIME_FORMATTER)
                    : message.getCreatedAt().format(TIME_FORMATTER));
            // 原样回填 clientMsgId：前端用它定位并替换自己的乐观气泡，避免重复显示（BUG-A 修复）
            ack.setClientMsgId(frame.getClientMsgId());
            wsSessionManager.sendToSession(userId, session, ack);
        } catch (BusinessException e) {
            sendError(session, userId, e.getMessage());
        } catch (Exception e) {
            log.warn("WebSocket 发送私信失败", e);
            sendError(session, userId, "消息发送失败，请重试");
        }
    }

    /**
     * 连接关闭：从在线表移除。
     *
     * @param session 会话
     */
    @OnClose
    public void onClose(Session session) {
        Long userId = SESSION_USERS.remove(session.getId());
        if (userId != null) {
            wsSessionManager.remove(userId, session);
        }
    }

    /**
     * 连接异常：仅记录日志，由容器负责关闭。
     *
     * @param session 会话
     * @param error   异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        Long userId = SESSION_USERS.get(session.getId());
        log.debug("WebSocket 异常：userId={}, err={}", userId, error.getMessage());
    }

    /**
     * 从握手参数取 token 并解析 userId。
     *
     * @param session 会话
     * @return userId 或 null
     */
    private Long resolveUserId(Session session) {
        if (session == null || session.getRequestParameterMap() == null || jwtUtil == null) {
            return null;
        }
        List<String> tokens = session.getRequestParameterMap().get("token");
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        return jwtUtil.parseUserIdQuietly(tokens.get(0));
    }

    /**
     * 关闭连接。
     *
     * @param session 会话
     * @param reason  关闭原因
     */
    private void closeQuietly(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            log.debug("关闭 WebSocket 失败：{}", e.getMessage());
        }
    }

    /**
     * 推送错误帧（不断连，便于前端改后重发）。
     *
     * @param session 会话
     * @param userId  用户 ID
     * @param message 错误文案
     */
    private void sendError(Session session, Long userId, String message) {
        WsMessage frame = new WsMessage();
        frame.setType(WsMessageType.ERROR.name());
        frame.setContent(message);
        frame.setTimestamp(LocalDateTime.now().format(TIME_FORMATTER));
        wsSessionManager.sendToSession(userId, session, frame);
    }

    /**
     * 字符串 ID 转 Long，非法返回 null。
     *
     * @param value 字符串
     * @return Long 或 null
     */
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
