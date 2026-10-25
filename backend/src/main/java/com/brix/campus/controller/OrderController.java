package com.brix.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.dto.CancelOrderDTO;
import com.brix.campus.model.dto.OrderCreateDTO;
import com.brix.campus.model.dto.PayDTO;
import com.brix.campus.model.dto.ReviewCreateDTO;
import com.brix.campus.model.dto.TradeCodeVerifyDTO;
import com.brix.campus.model.query.OrderQuery;
import com.brix.campus.model.vo.OrderDetailVO;
import com.brix.campus.model.vo.OrderVO;
import com.brix.campus.model.vo.ReviewVO;
import com.brix.campus.model.vo.TradeCodeVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.OrderService;
import com.brix.campus.service.ReviewService;
import com.brix.campus.service.TradeCodeService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口（E01-E09、F02-F04、G01）。
 */
@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final TradeCodeService tradeCodeService;
    private final ReviewService reviewService;

    public OrderController(OrderService orderService,
                           TradeCodeService tradeCodeService,
                           ReviewService reviewService) {
        this.orderService = orderService;
        this.tradeCodeService = tradeCodeService;
        this.reviewService = reviewService;
    }

    /**
     * 下单。
     *
     * @param dto 入参
     * @return 订单（UNPAID，前端跳收银台）
     */
    @PostMapping
    public R<OrderVO> create(@RequestBody @Valid OrderCreateDTO dto) {
        return R.ok(orderService.create(SecurityUtils.currentUserId(), dto), "下单成功");
    }

    /**
     * 收银台支付（模拟）。
     *
     * @param id  订单 ID
     * @param dto 支付方式
     * @return 支付后的订单
     */
    @PostMapping("/{id}/pay")
    public R<OrderVO> pay(@PathVariable("id") Long id, @RequestBody @Valid PayDTO dto) {
        return R.ok(orderService.pay(SecurityUtils.currentUserId(), id, dto), "支付成功");
    }

    /**
     * 卖家发货。
     *
     * @param id 订单 ID
     * @return 订单
     */
    @PostMapping("/{id}/ship")
    public R<OrderVO> ship(@PathVariable("id") Long id) {
        return R.ok(orderService.ship(SecurityUtils.currentUserId(), id), "已发货");
    }

    /**
     * 买家确认收货。
     *
     * @param id 订单 ID
     * @return 订单
     */
    @PostMapping("/{id}/confirm")
    public R<OrderVO> confirm(@PathVariable("id") Long id) {
        return R.ok(orderService.confirm(SecurityUtils.currentUserId(), id), "确认收货成功");
    }

    /**
     * 取消订单。
     *
     * @param id  订单 ID
     * @param dto 取消原因
     * @return 订单
     */
    @PostMapping("/{id}/cancel")
    public R<OrderVO> cancel(@PathVariable("id") Long id,
                             @RequestBody(required = false) @Valid CancelOrderDTO dto) {
        CancelOrderDTO body = dto == null ? new CancelOrderDTO() : dto;
        return R.ok(orderService.cancel(SecurityUtils.currentUserId(), id, body), "订单已取消");
    }

    /**
     * 我买到的订单。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/bought")
    public R<IPage<OrderVO>> bought(@Valid OrderQuery query) {
        return R.ok(orderService.pageBought(SecurityUtils.currentUserId(), query));
    }

    /**
     * 我卖出的订单。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/sold")
    public R<IPage<OrderVO>> sold(@Valid OrderQuery query) {
        return R.ok(orderService.pageSold(SecurityUtils.currentUserId(), query));
    }

    /**
     * 订单详情。
     *
     * @param id 订单 ID
     * @return 详情
     */
    @GetMapping("/{id}")
    public R<OrderDetailVO> detail(@PathVariable("id") Long id) {
        return R.ok(orderService.detail(SecurityUtils.currentUserId(),
                SecurityUtils.currentRoleOrNull(), id));
    }

    /**
     * 查看面交确认码（仅买卖双方）。
     *
     * @param id 订单 ID
     * @return 确认码信息
     */
    @GetMapping("/{id}/trade-code")
    public R<TradeCodeVO> viewTradeCode(@PathVariable("id") Long id) {
        return R.ok(tradeCodeService.view(SecurityUtils.currentUserId(), id));
    }

    /**
     * 卖家核销确认码。
     *
     * @param id  订单 ID
     * @param dto 6 位确认码
     * @return 核销结果
     */
    @PostMapping("/{id}/trade-code/verify")
    public R<TradeCodeVO> verifyTradeCode(@PathVariable("id") Long id,
                                          @RequestBody @Valid TradeCodeVerifyDTO dto) {
        return R.ok(tradeCodeService.verify(SecurityUtils.currentUserId(), id, dto.getCode()),
                "核销成功，交易完成");
    }

    /**
     * 买家评价卖家。
     *
     * @param id  订单 ID
     * @param dto 入参
     * @return 评价
     */
    @PostMapping("/{id}/review")
    public R<ReviewVO> review(@PathVariable("id") Long id, @RequestBody @Valid ReviewCreateDTO dto) {
        return R.ok(reviewService.create(SecurityUtils.currentUserId(), id, dto), "评价成功");
    }
}
