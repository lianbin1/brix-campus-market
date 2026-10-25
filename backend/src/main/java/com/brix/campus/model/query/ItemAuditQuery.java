package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台商品审核查询条件：默认查 {@code PENDING}，按提交时间升序（先到先审，K07）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemAuditQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 状态，默认待审核 */
    private String status = "PENDING";

    /** 关键词：标题 */
    private String keyword;
}
