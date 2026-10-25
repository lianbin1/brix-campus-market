package com.brix.campus.controller.admin;

import com.brix.campus.common.R;
import com.brix.campus.model.vo.DashboardOverviewVO;
import com.brix.campus.model.vo.TrendPointVO;
import com.brix.campus.service.DashboardService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台数据大屏（K02-K04，仅 OPERATOR）。
 */
@RestController
@RequestMapping("/admin/dashboard")
@Validated
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 四项核心指标。
     *
     * @return 指标
     */
    @GetMapping("/overview")
    public R<DashboardOverviewVO> overview() {
        return R.ok(dashboardService.overview());
    }

    /**
     * 近 N 天订单趋势（缺日补 0）。
     *
     * @param days 天数，默认 7
     * @return 连续数据点
     */
    @GetMapping("/order-trend")
    public R<List<TrendPointVO>> orderTrend(@RequestParam(value = "days", defaultValue = "7") int days) {
        return R.ok(dashboardService.orderTrend(days));
    }

    /**
     * 分类分布 + 待审核 Top10。
     *
     * @return {categories, pendingItems}
     */
    @GetMapping("/category-distribution")
    public R<Map<String, Object>> categoryDistribution() {
        return R.ok(dashboardService.categoryDistribution());
    }
}
