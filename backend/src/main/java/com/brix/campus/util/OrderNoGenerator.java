package com.brix.campus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成器：{@code yyyyMMddHHmmss} + 6 位随机数，共 20 位。
 *
 * <p>库上另有 {@code uk_order_no} 唯一索引兜底，极端碰撞由数据库拒绝并在全局异常处理里转为友好提示。</p>
 */
public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoGenerator() {
    }

    /**
     * 生成订单号。
     *
     * @return 20 位订单号
     */
    public static String generate() {
        String prefix = LocalDateTime.now().format(FORMATTER);
        int suffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return prefix + suffix;
    }
}
