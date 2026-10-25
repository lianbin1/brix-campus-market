package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 面交确认码核销入参（F03）。用 String 承载 6 位数字，保留前导 0。
 */
@Data
public class TradeCodeVerifyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 6 位纯数字确认码 */
    @NotBlank(message = "确认码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "确认码必须为 6 位数字")
    private String code;
}
