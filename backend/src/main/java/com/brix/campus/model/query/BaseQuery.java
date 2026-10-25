package com.brix.campus.model.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基类：{@code currentPage} 从 1 开始，{@code pageSize} 默认 10、最大 50。
 *
 * <p>查询条件对象与实体严格分离，<b>绝不复用实体当查询对象</b>。</p>
 */
@Data
public class BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页，从 1 开始 */
    @Min(value = 1, message = "页码最小为 1")
    private Integer currentPage = 1;

    /** 每页条数，默认 10，最大 50 */
    @Max(value = 50, message = "每页最多 50 条")
    private Integer pageSize = 10;

    /**
     * 计算偏移量（供 XML 手写分页 SQL 使用）。
     *
     * @return 偏移行数
     */
    public long offset() {
        int page = currentPage == null ? 1 : currentPage;
        int size = pageSize == null ? 10 : pageSize;
        return (long) (page - 1) * size;
    }
}
