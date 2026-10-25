package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.model.ChatMessage;
import com.brix.campus.model.query.AdminChatQuery;
import com.brix.campus.model.vo.AdminChatVO;
import com.brix.campus.model.vo.ConversationVO;
import com.brix.campus.model.vo.OrderChatMessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信 Mapper：会话列表按「对方用户 ID」聚合，必须写 XML（不建会话表）。
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 会话列表（按最后消息时间倒序）。
     *
     * @param uid 当前用户 ID
     * @return 会话列表
     */
    List<ConversationVO> selectConversations(@Param("uid") Long uid);

    /**
     * 全局未读数。
     *
     * @param uid 当前用户 ID
     * @return 未读条数
     */
    Long countUnread(@Param("uid") Long uid);

    /**
     * 把某会话中发给当前用户的消息置为已读。
     *
     * @param uid    当前用户 ID
     * @param peerId 对话方 ID
     * @return 影响行数
     */
    int markRead(@Param("uid") Long uid, @Param("peerId") Long peerId);

    /**
     * 取两个用户之间的消息（可按商品过滤），按发送时间升序。
     *
     * @param userA  用户 A（订单买卖双方之一）
     * @param userB  用户 B（订单买卖双方之另一）
     * @param itemId 商品 ID；为 {@code null} 时不按商品过滤（退化为全部消息）
     * @return 消息列表（升序）
     */
    List<OrderChatMessageVO> selectPairMessages(@Param("userA") Long userA,
                                                @Param("userB") Long userB,
                                                @Param("itemId") Long itemId);

    /**
     * 后台全平台私信分页检索（只读）。
     *
     * @param page  分页对象
     * @param query 查询条件（内容关键词 / 参与方用户名或昵称）
     * @return 分页结果
     */
    IPage<AdminChatVO> selectAdminPage(Page<AdminChatVO> page, @Param("q") AdminChatQuery query);
}
