package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.ChatMessageMapper;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.ChatMessage;
import com.brix.campus.model.Order;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ChatSendDTO;
import com.brix.campus.model.enums.UserStatusEnum;
import com.brix.campus.model.query.AdminChatQuery;
import com.brix.campus.model.query.ChatQuery;
import com.brix.campus.model.vo.AdminChatVO;
import com.brix.campus.model.vo.ChatMessageVO;
import com.brix.campus.model.vo.ChatUserVO;
import com.brix.campus.model.vo.ConversationVO;
import com.brix.campus.model.vo.OrderChatMessageVO;
import com.brix.campus.model.vo.OrderChatVO;
import com.brix.campus.service.ChatService;
import com.brix.campus.ws.WsMessage;
import com.brix.campus.ws.WsMessageType;
import com.brix.campus.ws.WsSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 私信服务实现。
 *
 * <p>落库时机：先 insert 拿到 id 再推送，保证刷新后历史完整（架构 7.4）。</p>
 *
 * <p><b>不可撤回 / 不可删除</b>：本类与 {@code ChatController} 均不提供删除或撤回能力，
 * 后台仅以只读接口查看，用于交易纠纷取证（K13）。</p>
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 后台私信检索每页上限（防御性收敛，与 BaseQuery 的 @Max 一致） */
    private static final long MAX_PAGE_SIZE = 50L;

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final WsSessionManager wsSessionManager;

    public ChatServiceImpl(ChatMessageMapper chatMessageMapper,
                           UserMapper userMapper,
                           OrderMapper orderMapper,
                           WsSessionManager wsSessionManager) {
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.wsSessionManager = wsSessionManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO send(Long fromUserId, ChatSendDTO dto) {
        if (dto.getToUserId() == null) {
            throw BusinessException.param("接收人不能为空");
        }
        if (dto.getToUserId().equals(fromUserId)) {
            throw BusinessException.param("不能给自己发消息");
        }
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.isEmpty()) {
            throw BusinessException.param("内容不能为空");
        }
        if (content.length() > 500) {
            throw BusinessException.param("消息内容不能超过 500 个字");
        }
        User receiver = userMapper.selectById(dto.getToUserId());
        if (receiver == null) {
            throw BusinessException.param("接收人不存在");
        }
        if (UserStatusEnum.DISABLED.getCode().equals(receiver.getStatus())) {
            throw BusinessException.param("接收人账号已被禁用");
        }

        ChatMessage message = new ChatMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(dto.getToUserId());
        message.setItemId(dto.getItemId());
        message.setContent(content);
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        ChatMessageVO vo = toVO(message);

        // 在线才推送；离线仅落库，由历史消息接口补齐
        WsMessage frame = new WsMessage();
        frame.setType(WsMessageType.CHAT.name());
        frame.setFromUserId(String.valueOf(fromUserId));
        frame.setToUserId(String.valueOf(message.getToUserId()));
        frame.setItemId(message.getItemId() == null ? null : String.valueOf(message.getItemId()));
        frame.setContent(message.getContent());
        frame.setMessageId(String.valueOf(message.getId()));
        frame.setTimestamp(message.getCreatedAt() == null
                ? null : message.getCreatedAt().format(TIME_FORMATTER));
        wsSessionManager.sendTo(message.getToUserId(), frame);

        return vo;
    }

    @Override
    public List<ConversationVO> conversations(Long userId) {
        List<ConversationVO> list = chatMessageMapper.selectConversations(userId);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public IPage<ChatMessageVO> messages(Long userId, ChatQuery query) {
        if (query.getPeerId() == null) {
            throw BusinessException.param("请指定对话方");
        }
        long current = query.getCurrentPage() == null ? 1L : query.getCurrentPage();
        long size = query.getPageSize() == null ? 20L : query.getPageSize();
        Page<ChatMessage> page = new Page<>(current, size);

        Long peerId = query.getPeerId();
        IPage<ChatMessage> messagePage = chatMessageMapper.selectPage(page, Wrappers.<ChatMessage>lambdaQuery()
                .and(w -> w
                        .nested(n -> n.eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, peerId))
                        .or()
                        .nested(n -> n.eq(ChatMessage::getFromUserId, peerId).eq(ChatMessage::getToUserId, userId)))
                .orderByAsc(ChatMessage::getId));

        Page<ChatMessageVO> voPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(),
                messagePage.getTotal());
        List<ChatMessageVO> records = new ArrayList<>(messagePage.getRecords().size());
        for (ChatMessage message : messagePage.getRecords()) {
            records.add(toVO(message));
        }
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public long unreadCount(Long userId) {
        Long count = chatMessageMapper.countUnread(userId);
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long markRead(Long userId, Long peerId) {
        if (peerId == null) {
            throw BusinessException.param("请指定对话方");
        }
        chatMessageMapper.markRead(userId, peerId);
        return unreadCount(userId);
    }

    /**
     * 实体转 VO。
     *
     * @param message 实体
     * @return VO
     */
    private ChatMessageVO toVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setItemId(message.getItemId());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    @Override
    public OrderChatVO orderChat(Long orderId) {
        Order order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }

        OrderChatVO vo = new OrderChatVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setItemTitle(order.getItemTitle());
        vo.setBuyer(toChatUser(order.getBuyerId()));
        vo.setSeller(toChatUser(order.getSellerId()));

        // ① 先取「围绕该商品」的消息；② 该商品下没有带 item_id 的消息则退化为二人全部消息
        List<OrderChatMessageVO> messages = chatMessageMapper.selectPairMessages(
                order.getSellerId(), order.getBuyerId(), order.getItemId());
        if (messages == null || messages.isEmpty()) {
            messages = chatMessageMapper.selectPairMessages(
                    order.getSellerId(), order.getBuyerId(), null);
        }
        List<OrderChatMessageVO> result = messages == null ? new ArrayList<>() : messages;
        vo.setMessages(result);
        vo.setTotal(result.size());
        return vo;
    }

    @Override
    public IPage<AdminChatVO> adminPage(AdminChatQuery query) {
        long current = query.getCurrentPage() == null ? 1L : query.getCurrentPage();
        long size = query.getPageSize() == null ? 10L : query.getPageSize();
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        Page<AdminChatVO> page = new Page<>(current, size);
        return chatMessageMapper.selectAdminPage(page, query);
    }

    /**
     * 取用户精简信息（后台取证展示用），用户不存在返回 null。
     *
     * @param userId 用户 ID
     * @return 精简用户信息或 null
     */
    private ChatUserVO toChatUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        ChatUserVO vo = new ChatUserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setUsername(user.getUsername());
        return vo;
    }
}
