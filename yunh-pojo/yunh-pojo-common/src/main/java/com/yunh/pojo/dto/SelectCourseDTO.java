package com.yunh.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 选课请求 DTO
 */
@Data
public class SelectCourseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 课程 ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 支付方式：1-微信 2-支付宝
     */
    @NotNull(message = "支付方式不能为空")
    @Positive(message = "支付方式必须为正整数")
    private Integer payType;
}
