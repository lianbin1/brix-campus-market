package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量审核通过入参（K08）：单条也走同一接口。
 */
@Data
public class BatchAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品 ID 列表 */
    @NotEmpty(message = "请选择要审核的商品")
    private List<Long> ids;
}
