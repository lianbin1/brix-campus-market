package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 私信查询条件：{@code peerId} 为对话方用户 ID；历史消息默认每页 20 条（PRD I03）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 对话方用户 ID */
    private Long peerId;

    /**
     * 私信默认每页 20 条（覆盖 {@link BaseQuery} 的 10）。
     *
     * <p>Spring 绑定请求参数时只会覆盖「请求里显式传入」的值，故缺省即 20。</p>
     */
    public ChatQuery() {
        setPageSize(20);
    }
}
