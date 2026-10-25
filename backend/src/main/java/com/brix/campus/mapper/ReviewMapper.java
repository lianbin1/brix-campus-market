package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brix.campus.model.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价 Mapper。
 */
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 卖家平均评分（保留一位小数）。
     *
     * @param sellerId 卖家 ID
     * @return 平均分，无评价返回 null
     */
    @Select("SELECT AVG(rating) FROM t_review WHERE seller_id = #{sellerId}")
    Double selectAvgRating(@Param("sellerId") Long sellerId);
}
