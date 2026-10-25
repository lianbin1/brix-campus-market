package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改个人资料入参（A04）。用户 ID 取自 token，请求体不接收 id。
 */
@Data
public class UpdateProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称不能超过 20 个字")
    private String nickname;

    /** 学校 */
    @Size(max = 100, message = "学校名称过长")
    private String school;

    /** 校区 */
    @Size(max = 50, message = "校区名称过长")
    private String campus;

    /** 手机号：11 位数字 */
    @Pattern(regexp = "^$|^\\d{11}$", message = "手机号必须为 11 位数字")
    private String phone;

    /** 简介 */
    @Size(max = 255, message = "简介不能超过 255 个字")
    private String bio;
}
