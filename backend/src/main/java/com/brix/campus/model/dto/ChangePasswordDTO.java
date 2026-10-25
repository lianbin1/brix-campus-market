package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码入参（A05）：必须提交旧密码。
 */
@Data
public class ChangePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 旧密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度不能少于 6 位")
    private String newPassword;
}
