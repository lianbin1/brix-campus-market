package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核拒绝入参（K09）：拒绝原因必填，1~200 字。
 */
@Data
public class AuditRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 拒绝原因 */
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 200, message = "拒绝原因不能超过 200 个字")
    private String reason;
}
