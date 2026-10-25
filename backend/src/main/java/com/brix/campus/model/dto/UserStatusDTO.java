package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户禁用 / 启用入参（K06）：{@code status} 取 NORMAL 或 DISABLED。
 */
@Data
public class UserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标状态：NORMAL / DISABLED */
    @NotBlank(message = "状态不能为空")
    private String status;
}
