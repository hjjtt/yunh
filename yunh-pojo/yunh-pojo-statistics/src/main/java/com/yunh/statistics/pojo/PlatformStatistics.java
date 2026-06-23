package com.yunh.statistics.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 平台统计实体类
 */
@Data
@TableName("t_platform_statistics")
public class PlatformStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate statDate;
    
    private Integer newUserCount;
    
    private Integer activeUserCount;
    
    private Integer newOrderCount;
    
    private BigDecimal totalIncome;
    
    private Integer newCourseCount;
    
    private Integer totalVideoViews;
}
