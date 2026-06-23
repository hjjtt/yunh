package com.yunh.statistics.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 课程统计实体类
 */
@Data
@TableName("t_course_statistics")
public class CourseStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long courseId;
    
    private LocalDate statDate;
    
    private Integer viewCount;
    
    private Integer buyCount;
    
    private BigDecimal income;
    
    private Integer commentCount;
    
    private Integer studentCount;
}
