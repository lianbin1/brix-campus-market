package com.brix.campus.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线会话管理（单实例内存态，架构 7.3）。
 *
 * <p>用 {@code Set} 而非单个 Session，支持同一用户多标签页；集合为空时移除 key 防泄漏。</p>
 */
@Component
public class WsSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WsSessionManager.class);

    /** userId → 该用户的全部在线会话 */
    private final Map<Long, Set<Session>> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public WsSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 注册会话。
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void add(Long userId, Session session) {
        sessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * 移除会话（集合空时移除 key）。
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void remove(Long userId, Session session) {
        if (userId == null || session == null) {
            return;
        }
        Set<Session> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            sessions.remove(userId);
        }
    }

    /**
     * 判断用户是否在线。
     *
     * @param userId 用户 ID
     * @return 是否在线
     */
    public boolean isOnline(Long userId) {
        Set<Session> userSessions = sessions.get(userId);
        return userSessions != null && !userSessions.isEmpty();
    }

    /**
     * 向指定用户的全部会话推送消息（失败仅记日志并移除失效会话）。
     *
     * @param userId 接收者 ID
     * @param message 帧
     */
    public void sendTo(Long userId, WsMessage message) {
        if (userId == null) {
            return;
        }
        Set<Session> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.warn("WebSocket 消息序列化失败", e);
            return;
        }
        for (Session session : userSessions) {
            sendRaw(userId, session, payload);
        }
    }

    /**
     * 向单个会话发送原始 JSON 文本。
     *
     * @param userId  用户 ID
     * @param session 会话
     * @param payload JSON 文本
     */
    public void sendRaw(Long userId, Session session, String payload) {
        if (session == null || !session.isOpen()) {
            remove(userId, session);
            return;
        }
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(payload);
            }
        } catch (IOException e) {
            log.debug("推送失败，移除会话：userId={}, err={}", userId, e.getMessage());
            remove(userId, session);
        }
    }

    /**
     * 向单个会话发送帧对象。
     *
     * @param userId  用户 ID
     * @param session 会话
     * @param message 帧
     */
    public void sendToSession(Long userId, Session session, WsMessage message) {
        try {
            sendRaw(userId, session, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.warn("WebSocket 消息序列化失败", e);
        }
    }
}
