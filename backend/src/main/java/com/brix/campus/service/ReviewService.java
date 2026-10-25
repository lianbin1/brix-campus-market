package com.brix.campus.service;

import com.brix.campus.model.dto.ReviewCreateDTO;
import com.brix.campus.model.query.CommentQuery;
import com.brix.campus.model.vo.ReviewPageVO;
import com.brix.campus.model.vo.ReviewVO;

/**
 * 评价服务（G01-G03）。
 */
public interface ReviewService {

    /**
     * 提交评价：仅买家、仅已完成订单、每单一次，并在同一事务内更新卖家信用分。
     *
     * @param buyerId 买家 ID
     * @param orderId 订单 ID
     * @param dto     入参
     * @return 评价
     */
    ReviewVO create(Long buyerId, Long orderId, ReviewCreateDTO dto);

    /**
     * 某卖家的评价分页 + 平均分 / 总数。
     *
     * @param sellerId 卖家 ID
     * @param query    分页条件
     * @return 分页与汇总
     */
    ReviewPageVO pageBySeller(Long sellerId, CommentQuery query);
}
