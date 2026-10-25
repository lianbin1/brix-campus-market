package com.brix.campus.ai;

import com.brix.campus.mapper.ItemMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 数据访问层：把「同款成交 / 在售」查询集中在一处，供 {@link AiTools}（LLM 工具）与
 * {@link RuleBasedEstimator}（本地规则）复用，避免两套口径。
 */
@Component
public class AiRepository {

    private final ItemMapper itemMapper;

    public AiRepository(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    /**
     * 近 90 天同款成交价中位数（MySQL 无 median，取列表后 Java 侧计算）。
     *
     * @param keyword    标题关键词
     * @param categoryId 分类 ID（当前 SQL 未按分类过滤，保留参数以便二期扩展）
     * @return 中位数，无数据返回 0
     */
    public BigDecimal soldMedianPrice(String keyword, Long categoryId) {
        List<BigDecimal> amounts = querySoldAmounts(keyword);
        if (amounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sorted = new ArrayList<>(amounts);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        }
        BigDecimal left = sorted.get(size / 2 - 1);
        BigDecimal right = sorted.get(size / 2);
        return left.add(right).divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 同款成交笔数。
     *
     * @param keyword 标题关键词
     * @return 笔数
     */
    public int soldCount(String keyword) {
        return querySoldAmounts(keyword).size();
    }

    /**
     * 同款在售最低价。
     *
     * @param keyword    标题关键词
     * @param categoryId 分类 ID
     * @return 最低价，无数据返回 0
     */
    public BigDecimal onSaleLowestPrice(String keyword, Long categoryId) {
        BigDecimal lowest = itemMapper.selectOnSaleLowestPrice(normalize(keyword), categoryId);
        return lowest == null ? BigDecimal.ZERO : lowest;
    }

    /**
     * 查询成交金额列表。
     *
     * @param keyword 标题关键词
     * @return 金额列表（可能为空）
     */
    private List<BigDecimal> querySoldAmounts(String keyword) {
        List<BigDecimal> amounts = itemMapper.selectSoldAmounts(normalize(keyword));
        return amounts == null ? Collections.emptyList() : amounts;
    }

    /**
     * 关键词归一化：去空白，空串转 null（让 SQL 不加 LIKE 条件）。
     *
     * @param keyword 原值
     * @return 归一化结果
     */
    private String normalize(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
