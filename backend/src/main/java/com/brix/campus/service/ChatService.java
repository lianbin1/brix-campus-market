package com.brix.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.model.dto.ChatSendDTO;
import com.brix.campus.model.query.AdminChatQuery;
import com.brix.campus.model.query.ChatQuery;
import com.brix.campus.model.vo.AdminChatVO;
import com.brix.campus.model.vo.ChatMessageVO;
import com.brix.campus.model.vo.ConversationVO;
import com.brix.campus.model.vo.OrderChatVO;

import java.util.List;

/**
 * 私信服务（I01-I04）：HTTP 兜底通道与 WebSocket 共用同一发送方法。
 */
public interface ChatService {

    /**
     * 发送私信：先落库再实时推送（离线仅落库）。
     *
     * @param fromUserId 发送者 ID
     * @param dto        入参
     * @return 落库后的消息
     */
    ChatMessageVO send(Long fromUserId, ChatSendDTO dto);

    /**
     * 会话列表：按对方用户聚合，按最后消息时间倒序。
     *
     * @param userId 当前用户 ID
     * @return 会话列表
     */
    List<ConversationVO> conversations(Long userId);

    /**
     * 历史消息：双向、按 id 升序分页。
     *
     * @param userId 当前用户 ID
     * @param query  查询条件（peerId 必填）
     * @return 分页结果
     */
    IPage<ChatMessageVO> messages(Long userId, ChatQuery query);

    /**
     * 全局未读数。
     *
     * @param userId 当前用户 ID
     * @return 未读条数
     */
    long unreadCount(Long userId);

    /**
     * 打开会话：把对方发来的消息置为已读，返回清零后的全局未读数。
     *
     * @param userId 当前用户 ID
     * @param peerId 对话方 ID
     * @return 清零后的全局未读数
     */
    long markRead(Long userId, Long peerId);

    /**
     * 后台查看某个订单买卖双方的私信记录（纠纷取证，只读）。
     *
     * <p>优先返回「围绕该商品」的消息；该商品下没有带 {@code item_id} 的消息时，
     * 退化为返回二人之间的全部消息。无消息返回空列表，不抛 404。</p>
     *
     * @param orderId 订单 ID
     * @return 订单维度会话
     */
    OrderChatVO orderChat(Long orderId);

    /**
     * 后台全平台私信分页检索（只读）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminChatVO> adminPage(AdminChatQuery query);
}
