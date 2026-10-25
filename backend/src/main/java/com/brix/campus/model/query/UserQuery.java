package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台用户查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 关键词：用户名或昵称 */
    private String keyword;

    /** 角色筛选 */
    private String role;

    /** 状态筛选 */
    private String status;
}
