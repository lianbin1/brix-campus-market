package com.brix.campus.security;

import com.brix.campus.common.BusinessException;
import com.brix.campus.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 SecurityContext 取当前登录用户的工具类。
 *
 * <p>数据归属校验一律以此处的 userId 为准，<b>忽略请求体里传入的 userId</b>（PRD 7.4）。</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 取当前登录用户，未登录抛 401。
     *
     * @return 登录态用户
     */
    public static LoginUser currentUser() {
        LoginUser loginUser = currentUserOrNull();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未认证或登录已过期");
        }
        return loginUser;
    }

    /**
     * 取当前登录用户，未登录返回 null（用于「带 token 时补充字段」的公开接口）。
     *
     * @return 登录态用户或 null
     */
    public static LoginUser currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 当前用户 ID，未登录抛 401。
     *
     * @return 用户 ID
     */
    public static Long currentUserId() {
        return currentUser().getId();
    }

    /**
     * 当前用户 ID，未登录返回 null。
     *
     * @return 用户 ID 或 null
     */
    public static Long currentUserIdOrNull() {
        LoginUser loginUser = currentUserOrNull();
        return loginUser == null ? null : loginUser.getId();
    }

    /**
     * 当前用户角色码，未登录返回 null。
     *
     * @return 角色码或 null
     */
    public static String currentRoleOrNull() {
        LoginUser loginUser = currentUserOrNull();
        return loginUser == null ? null : loginUser.getRole();
    }
}
