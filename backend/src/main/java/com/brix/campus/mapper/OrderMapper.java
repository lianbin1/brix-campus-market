package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.model.Order;
import com.brix.campus.model.query.OrderQuery;
import com.brix.campus.model.vo.AdminOrderVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper：单表用 {@link BaseMapper}；统计与后台联表走 XML。
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 后台全平台订单分页（状态 / 关键词 / 买卖家用户名）。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminOrderVO> selectAdminPage(Page<AdminOrderVO> page, @Param("q") OrderQuery query);

    /**
     * 后台单条订单（与 {@link #selectAdminPage} 同一投影，用于运营操作后回显最新数据）。
     *
     * @param id 订单 ID
     * @return VO，不存在返回 null
     */
    AdminOrderVO selectAdminById(@Param("id") Long id);

    /**
     * 累计成交额：仅统计已支付及之后的订单状态（PRD 8-#6）。
     *
     * @return 金额，无数据返回 0
     */
    BigDecimal sumPaidAmount();

    /**
     * 今日订单数。
     *
     * @return 数量
     */
    Long countToday();

    /**
     * 按天统计订单数（趋势图用，缺日由 Java 侧补 0）。
     *
     * @param start 起始日期（含）
     * @param end   结束日期（含）
     * @return 每行包含 date / count
     */
    List<Map<String, Object>> countDailyOrders(@Param("start") LocalDate start,
                                               @Param("end") LocalDate end);
}
