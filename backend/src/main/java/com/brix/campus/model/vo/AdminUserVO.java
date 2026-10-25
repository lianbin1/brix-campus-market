package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台用户列表项（K05）。
 */
@Data
public class AdminUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 登录名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 学校 */
    private String school;

    /** 校区 */
    private String campus;

    /** 角色码 */
    private String role;

    /** 角色文案 */
    private String roleDesc;

    /** 状态码 */
    private String status;

    /** 状态文案 */
    private String statusDesc;

    /** 信用分 */
    private Integer creditScore;

    /** 注册时间 */
    private LocalDateTime createdAt;

    /** 禁用时间 */
    private LocalDateTime disabledAt;
}
