package com.brix.campus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.query.OrderQuery;
import com.brix.campus.model.vo.AdminOrderVO;
import com.brix.campus.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台全平台订单查询与运营兜底处理（K11 + BUG-C，仅 OPERATOR）。
 *
 * <p>权限：URL 层 {@code /admin/orders/**} 已在 {@code SecurityConfig} 中限定 {@code hasRole("OPERATOR")}，
 * Controller 内不再写角色判断（规范：Controller 不写业务判断）。</p>
 */
@RestController
@RequestMapping("/admin/orders")
@Validated
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 订单列表（状态 + 关键词：订单号 / 商品标题 / 买卖家用户名）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public R<IPage<AdminOrderVO>> list(@Valid OrderQuery query) {
        return R.ok(orderService.adminPage(query));
    }

    /**
     * 重置面交核销失败次数：仅 {@code TO_MEET} 且已锁定（失败次数达到上限）可执行。
     *
     * @param id 订单 ID
     * @return 重置后的最新订单
     */
    @PostMapping("/{id}/reset-trade-code")
    public R<AdminOrderVO> resetTradeCode(@PathVariable("id") Long id) {
        return R.ok(orderService.resetTradeCode(id), "已重置核销次数");
    }

    /**
     * 强制取消订单：仅 {@code TO_MEET} / {@code TO_SHIP} 可执行，商品已售出时回滚为在售。
     *
     * @param id 订单 ID
     * @return 取消后的最新订单
     */
    @PostMapping("/{id}/force-cancel")
    public R<AdminOrderVO> forceCancel(@PathVariable("id") Long id) {
        return R.ok(orderService.forceCancel(id), "订单已强制取消");
    }
}
