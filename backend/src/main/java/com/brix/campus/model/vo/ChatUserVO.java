package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 私信参与方精简信息（后台取证展示用）。
 *
 * <p>只暴露 id / 昵称 / 用户名，不含手机号、校区等隐私字段。</p>
 */
@Data
public class ChatUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID（序列化后前端为 string） */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 用户名（登录名） */
    private String username;
}
