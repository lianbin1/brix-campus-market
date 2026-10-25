package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.brix.campus.mapper.CategoryMapper;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Category;
import com.brix.campus.model.Item;
import com.brix.campus.model.User;
import com.brix.campus.model.enums.ItemStatusEnum;
import com.brix.campus.model.vo.AdminItemVO;
import com.brix.campus.model.vo.CategoryDistributionVO;
import com.brix.campus.model.vo.DashboardOverviewVO;
import com.brix.campus.model.vo.TrendPointVO;
import com.brix.campus.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏服务实现。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 待审核列表条数 */
    private static final int PENDING_TOP = 10;

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;

    public DashboardServiceImpl(UserMapper userMapper,
                                ItemMapper itemMapper,
                                OrderMapper orderMapper,
                                CategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.orderMapper = orderMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public DashboardOverviewVO overview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setUserCount(nullToZero(userMapper.selectCount(Wrappers.<User>lambdaQuery())));
        vo.setOnSaleCount(nullToZero(itemMapper.selectCount(Wrappers.<Item>lambdaQuery()
                .eq(Item::getStatus, ItemStatusEnum.ON_SALE.getCode()))));
        Long todayCount = orderMapper.countToday();
        vo.setTodayOrderCount(todayCount == null ? 0L : todayCount);
        BigDecimal amount = orderMapper.sumPaidAmount();
        vo.setTotalTradeAmount(amount == null ? BigDecimal.ZERO : amount);
        return vo;
    }

    @Override
    public List<TrendPointVO> orderTrend(int days) {
        int span = days <= 0 ? 7 : Math.min(days, 31);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(span - 1L);

        Map<String, Long> countMap = new HashMap<>();
        List<Map<String, Object>> rows = orderMapper.countDailyOrders(start, end);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object dateValue = row.get("date");
                Object countValue = row.get("count");
                if (dateValue != null) {
                    long count = countValue instanceof Number number ? number.longValue() : 0L;
                    countMap.put(dateValue.toString(), count);
                }
            }
        }

        List<TrendPointVO> points = new ArrayList<>(span);
        for (int i = 0; i < span; i++) {
            String date = start.plusDays(i).format(DATE_FORMATTER);
            points.add(new TrendPointVO(date, countMap.getOrDefault(date, 0L)));
        }
        return points;
    }

    @Override
    public Map<String, Object> categoryDistribution() {
        Map<Long, Long> onSaleMap = new HashMap<>();
        List<Map<String, Object>> rows = itemMapper.countOnSaleGroupByCategory();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object idValue = row.get("categoryId");
                Object countValue = row.get("cnt");
                if (idValue instanceof Number idNumber && countValue instanceof Number countNumber) {
                    onSaleMap.put(idNumber.longValue(), countNumber.longValue());
                }
            }
        }

        List<Category> categories = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        List<CategoryDistributionVO> distribution = new ArrayList<>(categories.size());
        for (Category category : categories) {
            distribution.add(new CategoryDistributionVO(category.getId(), category.getName(),
                    onSaleMap.getOrDefault(category.getId(), 0L)));
        }

        List<AdminItemVO> pendingItems = itemMapper.selectPendingTop(PENDING_TOP);
        if (pendingItems == null) {
            pendingItems = new ArrayList<>();
        }
        pendingItems.forEach(item -> item.setStatusDesc(ItemStatusEnum.descOf(item.getStatus())));

        Map<String, Object> result = new HashMap<>(4);
        result.put("categories", distribution);
        result.put("pendingItems", pendingItems);
        return result;
    }

    /**
     * null 转 0。
     *
     * @param value 原值
     * @return 非 null 值
     */
    private Long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
