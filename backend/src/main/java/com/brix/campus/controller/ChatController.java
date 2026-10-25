package com.brix.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.dto.ChatSendDTO;
import com.brix.campus.model.query.ChatQuery;
import com.brix.campus.model.vo.ChatMessageVO;
import com.brix.campus.model.vo.ConversationVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 私信接口（I01-I04）：WebSocket 的 HTTP 兜底通道。
 */
@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送消息（与 WebSocket 同一 Service 方法）。
     *
     * @param dto 入参
     * @return 落库后的消息
     */
    @PostMapping("/messages")
    public R<ChatMessageVO> send(@RequestBody @Valid ChatSendDTO dto) {
        return R.ok(chatService.send(SecurityUtils.currentUserId(), dto));
    }

    /**
     * 会话列表（按对方聚合，时间倒序）。
     *
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public R<List<ConversationVO>> conversations() {
        return R.ok(chatService.conversations(SecurityUtils.currentUserId()));
    }

    /**
     * 历史消息（双向，按 id 升序）。
     *
     * @param query 查询条件（peerId 必填）
     * @return 分页结果
     */
    @GetMapping("/messages")
    public R<IPage<ChatMessageVO>> messages(@Valid ChatQuery query) {
        return R.ok(chatService.messages(SecurityUtils.currentUserId(), query));
    }

    /**
     * 全局未读数。
     *
     * @return {count:n}
     */
    @GetMapping("/unread-count")
    public R<Map<String, Object>> unreadCount() {
        long count = chatService.unreadCount(SecurityUtils.currentUserId());
        return R.ok(Map.of("count", (int) count));
    }

    /**
     * 打开会话即已读。
     *
     * @param peerId 对话方 ID
     * @return 清零后的全局未读数 {count:n}
     */
    @PutMapping("/read")
    public R<Map<String, Object>> read(@RequestParam("peerId") Long peerId) {
        long count = chatService.markRead(SecurityUtils.currentUserId(), peerId);
        return R.ok(Map.of("count", (int) count));
    }
}
