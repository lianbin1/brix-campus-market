package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 举报处理入参（H02）：{@code action} 取 OFFLINE（下架商品）或 REJECT（驳回举报）。
 */
@Data
public class ReportHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 处理动作：OFFLINE / REJECT */
    @NotBlank(message = "处理动作不能为空")
    private String action;
}
