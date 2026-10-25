package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 举报查询条件（后台）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 状态筛选：PENDING / HANDLED，空=全部 */
    private String status;

    /** 商品 ID 筛选 */
    private Long itemId;
}
