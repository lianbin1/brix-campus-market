package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品上下架入参（B07）：{@code action} 取 {@code ON_SALE}（重新上架）或 {@code OFF_SHELF}（下架）。
 */
@Data
public class ItemStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标动作：ON_SALE / OFF_SHELF */
    @NotBlank(message = "操作类型不能为空")
    private String action;
}
