package com.brix.campus.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 后台菜单项（登录时按角色返回，前端按 role 本地重建同一份常量）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单唯一键 */
    private String key;

    /** 菜单标题 */
    private String title;

    /** 前端路由 path */
    private String path;

    /** Element Plus 图标名 */
    private String icon;
}
