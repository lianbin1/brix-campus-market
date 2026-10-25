package com.brix.campus.service;

import com.brix.campus.model.vo.TradeCodeVO;

/**
 * 面交确认码服务（F02-F04）。
 */
public interface TradeCodeService {

    /**
     * 查看确认码：仅该单买卖双方可见。
     *
     * @param userId  当前用户 ID
     * @param orderId 订单 ID
     * @return 确认码信息（含失败次数 / 剩余次数 / 是否锁定）
     */
    TradeCodeVO view(Long userId, Long orderId);

    /**
     * 卖家核销确认码：失败次数持久化，累计 5 次后永久锁定入口。
     *
     * @param sellerId 操作人（卖家）ID
     * @param orderId  订单 ID
     * @param code     买家口述的 6 位码
     * @return 核销结果
     */
    TradeCodeVO verify(Long sellerId, Long orderId, String code);
}
