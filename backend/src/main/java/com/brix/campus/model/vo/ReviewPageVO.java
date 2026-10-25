package com.brix.campus.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;

/**
 * 卖家评价分页出参：分页数据 + 汇总（平均分 / 总数）。
 */
@Data
public class ReviewPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分页数据（records / total / current / size / pages） */
    private IPage<ReviewVO> page;

    /** 汇总信息 */
    private ReviewSummaryVO summary;
}
