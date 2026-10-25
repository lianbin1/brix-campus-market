package com.brix.campus.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消订单入参（E05）：原因可选，仅用于前端展示。
 */
@Data
public class CancelOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 取消原因 */
    @Size(max = 200, message = "取消原因不能超过 200 个字")
    private String reason;
}
