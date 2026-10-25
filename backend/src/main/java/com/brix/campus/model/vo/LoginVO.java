package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录出参（A02 / K01）：token + 用户信息 + 按角色过滤的后台菜单。
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT */
    private String token;

    /** 用户信息 */
    private UserVO user;

    /** 可用后台菜单（USER 为空列表） */
    private List<MenuVO> menus;
}
