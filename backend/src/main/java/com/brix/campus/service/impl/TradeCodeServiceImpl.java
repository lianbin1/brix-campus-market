package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.model.Order;
import com.brix.campus.model.enums.OrderStatusEnum;
import com.brix.campus.model.vo.TradeCodeVO;
import com.brix.campus.service.TradeCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 面交确认码服务实现。
 *
 * <p>失败次数落在 {@code t_order.code_fail_count}，<b>不在内存里</b>，因此重启服务后仍保持锁定（PRD F04）。</p>
 *
 * <p><b>事务关键点（BUG-1 修复）</b>：{@link #verify} 标注了 {@code @Transactional(rollbackFor = Exception.class)}，
 * 而「确认码错误」分支随后会抛 {@link BusinessException}（RuntimeException）触发整体回滚。
 * 若计数自增与抛异常同处一个事务，计数永远写不进库。因此失败计数自增必须用
 * {@code PROPAGATION_REQUIRES_NEW} 的<b>独立事务</b>先行提交，外层事务再回滚也不影响已落库的计数。</p>
 */
@Service
public class TradeCodeServiceImpl implements TradeCodeService {

    private static final Logger log = LoggerFactory.getLogger(TradeCodeServiceImpl.class);

    private final OrderMapper orderMapper;
    private final int maxFail;

    /** 独立新事务模板：失败计数自增专用（不受外层 verify 事务回滚影响） */
    private final TransactionTemplate requiresNewTx;

    public TradeCodeServiceImpl(OrderMapper orderMapper,
                                @Value("${brix.trade-code.max-fail:5}") int maxFail,
                                PlatformTransactionManager transactionManager) {
        this.orderMapper = orderMapper;
        this.maxFail = maxFail;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNewTx = template;
    }

    @Override
    public TradeCodeVO view(Long userId, Long orderId) {
        Order order = requireOrder(orderId);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权查看该订单");
        }
        if (order.getTradeCode() == null || order.getTradeCode().isBlank()) {
            throw BusinessException.param("尚未生成确认码");
        }
        return buildVO(order, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeCodeVO verify(Long sellerId, Long orderId, String code) {
        Order order = requireOrder(orderId);
        if (!order.getSellerId().equals(sellerId)) {
            throw BusinessException.forbidden("仅卖家可核销确认码");
        }
        int failCount = order.getCodeFailCount() == null ? 0 : order.getCodeFailCount();
        if (failCount >= maxFail) {
            throw BusinessException.param("该订单核销已锁定，请联系运营处理");
        }
        if (!OrderStatusEnum.TO_MEET.getCode().equals(order.getStatus())) {
            throw BusinessException.param("当前订单状态不支持该操作");
        }
        if (order.getTradeCode() == null || order.getTradeCode().isBlank()) {
            throw BusinessException.param("尚未生成确认码");
        }

        if (!order.getTradeCode().equals(code == null ? "" : code.trim())) {
            // 失败计数必须落在独立新事务里先行提交：外层 verify 事务随后因抛异常回滚，
            // 但 code_fail_count 的自增已经持久化（BUG-1 修复，PRD F04 计数必须递减）。
            Integer latestFail = requiresNewTx.execute(status -> {
                Order fresh = requireOrder(orderId);
                int current = fresh.getCodeFailCount() == null ? 0 : fresh.getCodeFailCount();
                orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        // 原子自增，条件更新保证并发安全；即使服务重启计数仍在库中
                        .setSql("code_fail_count = code_fail_count + 1"));
                return current + 1;
            });
            int failCountAfter = latestFail == null ? 0 : latestFail;
            int remaining = Math.max(0, maxFail - failCountAfter);
            log.info("面交码核销失败：orderId={}, failCount={}", orderId, failCountAfter);
            throw BusinessException.param("确认码错误，剩余 " + remaining + " 次机会");
        }

        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatusEnum.TO_MEET.getCode())
                .set(Order::getStatus, OrderStatusEnum.DONE.getCode())
                .set(Order::getFinishTime, LocalDateTime.now()));
        if (rows == 0) {
            throw BusinessException.param("当前订单状态不支持该操作");
        }
        Order latest = requireOrder(orderId);
        return buildVO(latest, Boolean.TRUE);
    }

    /**
     * 取订单，不存在抛 404。
     *
     * @param orderId 订单 ID
     * @return 订单实体
     */
    private Order requireOrder(Long orderId) {
        Order order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        return order;
    }

    /**
     * 组装确认码出参。
     *
     * @param order   订单实体
     * @param success 核销结果（查看场景为 null）
     * @return VO
     */
    private TradeCodeVO buildVO(Order order, Boolean success) {
        int failCount = order.getCodeFailCount() == null ? 0 : order.getCodeFailCount();
        TradeCodeVO vo = new TradeCodeVO();
        vo.setTradeCode(order.getTradeCode());
        vo.setFailCount(failCount);
        vo.setRemainingAttempts(Math.max(0, maxFail - failCount));
        vo.setLocked(failCount >= maxFail);
        vo.setSuccess(success);
        vo.setOrderStatus(order.getStatus());
        vo.setOrderStatusDesc(OrderStatusEnum.descOf(order.getStatus()));
        return vo;
    }
}
