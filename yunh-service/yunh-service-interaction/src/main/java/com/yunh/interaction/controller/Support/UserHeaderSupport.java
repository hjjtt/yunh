package com.yunh.interaction.controller.support;

import com.yunh.common.exception.BusinessException;

public final class UserHeaderSupport {

    private UserHeaderSupport() {
    }

    public static Long requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new BusinessException("未获取到当前登录用户信息");
        }

        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("当前登录用户信息无效");
        }
    }

    /**
     * 解析用户ID，不抛异常，失败返回 null
     */
    public static Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
