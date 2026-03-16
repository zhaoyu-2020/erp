package com.mc.erp.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户信息（从 Spring Security Context 中获取）。
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户ID（由 JwtAuthenticationFilter 放入 Authentication.details）
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object details = auth.getDetails();
        if (details == null) {
            return null;
        }
        if (details instanceof Long) {
            return (Long) details;
        }
        if (details instanceof Integer) {
            return ((Integer) details).longValue();
        }
        try {
            return Long.valueOf(details.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal == null ? null : principal.toString();
    }

    /**
     * 判断当前登录用户是否为管理员（roleCode = admin）。
     * 直接读取 Spring Security 上下文中已加载的 authorities，无需额外查询数据库。
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_admin".equals(a.getAuthority()));
    }
}
