package com.brix.campus.common.validation;

/**
 * 校验分组：新增（Save）与更新（Update）场景复用同一 DTO 时区分必填项。
 */
public interface ValidGroup {

    /** 新增场景 */
    interface Save {
    }

    /** 更新场景 */
    interface Update {
    }
}
