package com.brix.campus.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台私信检索条件（K13，争议取证用）。
 *
 * <p>只读检索：按消息内容关键词 + 参与方用户名/昵称模糊匹配；分页沿用
 * {@link BaseQuery}（{@code currentPage} 从 1 开始，{@code pageSize} 最大 50）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminChatQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /** 消息内容关键词（模糊），空=不限 */
    private String keyword;

    /** 发送方或接收方的用户名 / 昵称关键词（模糊），空=不限 */
    private String username;
}
