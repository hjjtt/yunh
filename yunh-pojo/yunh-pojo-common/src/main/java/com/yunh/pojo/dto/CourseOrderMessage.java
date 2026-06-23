package com.yunh.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 选课订单消息体
 * 用于 MQ 消息传递
 */
@Data
public class CourseOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
