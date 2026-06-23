package com.yunh.common.constant;

/**
 * 内部服务调用常量
 */
public final class InternalCallConstant {

    /**
     * 内部调用标记 Header
     */
    public static final String HEADER_NAME = "X-Internal-Call";

    /**
     * 内部调用标记值
     */
    public static final String HEADER_VALUE = "YUNH_INTERNAL";

    private InternalCallConstant() {
    }
}
