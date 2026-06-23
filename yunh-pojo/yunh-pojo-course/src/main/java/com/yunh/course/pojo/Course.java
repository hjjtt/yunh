package com.yunh.course.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@TableName("t_course")
public class Course implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 讲师 ID
     */
    private Long teacherId;
    
    /**
     * 讲师名称
     */
    private String teacherName;
    
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 封面图
     */
    private String cover;
    
    /**
     * 状态 0-下架 1-上架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
