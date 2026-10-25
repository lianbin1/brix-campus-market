package com.brix.campus.job;

import com.brix.campus.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时关单定时任务（PRD E06）：每分钟扫描 {@code UNPAID} 且下单超过 30 分钟的订单，
 * 置为 {@code CANCELED} 并写 {@code close_reason=TIMEOUT}。
 */
@Component
public class OrderTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutJob.class);

    private final OrderService orderService;
    private final int unpaidTimeoutMinutes;

    public OrderTimeoutJob(OrderService orderService,
                           @Value("${brix.order.unpaid-timeout-minutes:30}") int unpaidTimeoutMinutes) {
        this.orderService = orderService;
        this.unpaidTimeoutMinutes = unpaidTimeoutMinutes;
    }

    /**
     * 每 1 分钟执行一次（固定延迟，避免任务堆积）。
     */
    @Scheduled(initialDelay = 60_000L, fixedDelay = 60_000L)
    public void closeTimeoutOrders() {
        try {
            int closed = orderService.closeTimeoutOrders(unpaidTimeoutMinutes);
            if (closed > 0) {
                log.info("超时关单完成：关闭 {} 笔待付款订单（阈值 {} 分钟）", closed, unpaidTimeoutMinutes);
            }
        } catch (Exception e) {
            log.error("超时关单任务失败", e);
        }
    }
}
