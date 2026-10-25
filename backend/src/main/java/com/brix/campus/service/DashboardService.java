package com.brix.campus.service;

import com.brix.campus.model.vo.DashboardOverviewVO;
import com.brix.campus.model.vo.TrendPointVO;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏服务（K02-K04）。
 */
public interface DashboardService {

    /**
     * 四项核心指标。
     *
     * @return 指标
     */
    DashboardOverviewVO overview();

    /**
     * 近 N 天订单趋势（含今天，缺日补 0）。
     *
     * @param days 天数，默认 7
     * @return 连续数据点
     */
    List<TrendPointVO> orderTrend(int days);

    /**
     * 分类分布 + 待审核 Top10。
     *
     * @return {categories:[...], pendingItems:[...]}
     */
    Map<String, Object> categoryDistribution();
}
