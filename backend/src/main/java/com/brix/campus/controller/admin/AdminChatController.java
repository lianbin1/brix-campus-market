package com.brix.campus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.query.AdminChatQuery;
import com.brix.campus.model.vo.AdminChatVO;
import com.brix.campus.model.vo.OrderChatVO;
import com.brix.campus.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台私信查看（K13，纠纷取证）。
 *
 * <p><b>只读接口</b>：私信不可撤回、不可删除，后台仅能查看并长期保留，避免交易纠纷无据可查。
 * 因此本类<b>不提供</b>任何删除 / 撤回 / 修改入口。</p>
 *
 * <p>权限：运营（OPERATOR）与审核员（REVIEWER）均可查看，
 * URL 层已在 {@code SecurityConfig} 中限定 {@code hasAnyRole("OPERATOR","REVIEWER")}。</p>
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminChatController {

    private final ChatService chatService;

    public AdminChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 某个订单买卖双方的私信记录（按发送时间升序，空对话返回空数组）。
     *
     * @param id 订单 ID
     * @return 订单维度会话
     */
    @GetMapping("/orders/{id}/chat")
    public R<OrderChatVO> orderChat(@PathVariable("id") Long id) {
        return R.ok(chatService.orderChat(id));
    }

    /**
     * 全平台私信分页检索（内容关键词 / 参与方用户名或昵称，默认时间倒序）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/chats")
    public R<IPage<AdminChatVO>> page(@Valid AdminChatQuery query) {
        return R.ok(chatService.adminPage(query));
    }
}
