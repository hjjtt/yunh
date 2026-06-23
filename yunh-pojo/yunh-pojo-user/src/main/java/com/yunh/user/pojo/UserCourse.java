package com.yunh.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户选课记录实体
 */
@Data
@TableName("t_user_course")
public class UserCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 状态：0-待支付 1-已支付 2-已取消
     */
    private Integer status;

    /**
     * 支付方式：1-微信 2-支付宝
     */
    private Integer payType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
