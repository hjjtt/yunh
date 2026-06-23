package com.yunh.statistics.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 平台统计实体类
 */
@Data
@TableName("t_platform_statistics")
public class PlatformStatistics {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 新增用户数
     */
    private Integer newUserCount;
    
    /**
     * 活跃用户数
     */
    private Integer activeUserCount;
    
    /**
     * 新增订单数
     */
    private Integer newOrderCount;
    
    /**
     * 总收入
     */
    private BigDecimal totalIncome;
    
    /**
     * 新增课程数
     */
    private Integer newCourseCount;
    
    /**
     * 视频播放总数
     */
    private Integer totalVideoViews;
    
    /**
     * 适配前端需要的date字段
     */
    public LocalDate getDate() {
        return this.statDate;
    }
}
