package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.model.Item;
import com.brix.campus.model.query.ItemAuditQuery;
import com.brix.campus.model.vo.AdminItemVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品 Mapper：单表用 {@link BaseMapper}；联表 / 聚合查询走 XML。
 */
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 各分类在售商品数（数据大屏饼图）。
     *
     * @return 每行包含 categoryId / name / cnt
     */
    List<Map<String, Object>> countOnSaleGroupByCategory();

    /**
     * 各分类商品总数（分类管理列表使用，全状态统计）。
     *
     * @return 每行包含 categoryId / cnt
     */
    List<Map<String, Object>> countItemGroupByCategory();

    /**
     * 同款在售最低价（AI 工具 onSaleLowestPrice）。
     *
     * @param keyword    标题关键词
     * @param categoryId 分类 ID
     * @return 最低价，无数据返回 null
     */
    BigDecimal selectOnSaleLowestPrice(@Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId);

    /**
     * 近 90 天同款成交金额列表（AI 工具 soldMedianPrice，中位数在 Java 侧计算）。
     *
     * @param keyword 标题关键词
     * @return 成交金额列表
     */
    List<BigDecimal> selectSoldAmounts(@Param("keyword") String keyword);

    /**
     * 后台审核分页（按提交时间升序，先到先审）。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminItemVO> selectAdminPage(Page<AdminItemVO> page, @Param("q") ItemAuditQuery query);

    /**
     * 待审核商品 Top N（数据大屏）。
     *
     * @param limit 条数
     * @return 列表
     */
    List<AdminItemVO> selectPendingTop(@Param("limit") int limit);
}
