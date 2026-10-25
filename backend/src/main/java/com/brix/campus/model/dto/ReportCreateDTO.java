package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交举报入参（H01）：原因必须为枚举，说明 0~200 字。
 */
@Data
public class ReportCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 原因：FAKE_INFO / CONTRABAND / PRICE_FRAUD / STOLEN_IMAGE / OTHER */
    @NotBlank(message = "举报原因不能为空")
    private String reason;

    /** 说明 0~200 字 */
    @Size(max = 200, message = "举报说明不能超过 200 个字")
    private String description;
}
