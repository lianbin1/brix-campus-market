package com.brix.campus;

import com.brix.campus.common.BusinessException;
import com.brix.campus.model.vo.TradeCodeVO;
import com.brix.campus.service.OrderService;
import com.brix.campus.service.TradeCodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 面交核销锁定测试（F03、F04）。
 *
 * <p>启用前提：先初始化数据库；种子订单 id=1 为 {@code TO_MEET} 状态，确认码 {@code 042317}。</p>
 */
@Disabled("需先初始化数据库（db/schema.sql + db/data.sql）并配置 DB_USERNAME/DB_PASSWORD 后手工启用")
@SpringBootTest
class TradeCodeLockTest {

    @Autowired
    private TradeCodeService tradeCodeService;

    @Autowired
    private OrderService orderService;

    @Test
    @Transactional
    @DisplayName("错误确认码：剩余次数递减，第 5 次后锁定且正确码也失败")
    void verify_wrongCodeFiveTimes_thenLocked() {
        Long orderId = 1L;
        Long sellerId = 8L;

        TradeCodeVO before = tradeCodeService.view(1L, orderId);
        Assertions.assertNotNull(before.getTradeCode(), "待面交订单必须已有确认码");
        Assertions.assertFalse(before.getLocked(), "初始状态不应锁定");

        for (int i = 1; i <= 5; i++) {
            Assertions.assertThrows(BusinessException.class,
                    () -> tradeCodeService.verify(sellerId, orderId, "000000"),
                    "第 " + i + " 次输入错误码必须抛业务异常");
        }

        TradeCodeVO after = tradeCodeService.view(1L, orderId);
        Assertions.assertTrue(after.getLocked(), "累计失败 5 次后必须锁定");
        Assertions.assertEquals(0, after.getRemainingAttempts(), "锁定后剩余次数应为 0");

        Assertions.assertThrows(BusinessException.class,
                () -> tradeCodeService.verify(sellerId, orderId, after.getTradeCode()),
                "锁定后即使输入正确码也必须失败");
    }

    @Test
    @Transactional
    @DisplayName("买家自行核销：抛越权异常")
    void verify_byBuyer_throwsForbidden() {
        Assertions.assertThrows(BusinessException.class,
                () -> tradeCodeService.verify(1L, 1L, "042317"),
                "买家核销必须被拒绝");
    }

    @Test
    @Transactional
    @DisplayName("正确确认码：订单流转为已完成")
    void verify_correctCode_finishesOrder() {
        TradeCodeVO result = tradeCodeService.verify(8L, 1L, "042317");
        Assertions.assertTrue(Boolean.TRUE.equals(result.getSuccess()), "正确码应核销成功");
        Assertions.assertEquals("DONE", result.getOrderStatus(), "核销后订单应为 DONE");
        Assertions.assertEquals("DONE", orderService.getEntity(1L).getStatus(), "库中订单状态应为 DONE");
    }
}
