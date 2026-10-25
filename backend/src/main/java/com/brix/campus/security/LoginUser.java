package com.brix.campus.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录态用户（放入 SecurityContext 的 principal）。
 *
 * <p>只承载鉴权必需字段，不含密码、手机号等敏感信息。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 登录名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色码：USER / REVIEWER / OPERATOR */
    private String role;
}
