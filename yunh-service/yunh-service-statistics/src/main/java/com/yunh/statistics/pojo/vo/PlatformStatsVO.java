package com.yunh.statistics.pojo.vo;

import com.yunh.statistics.pojo.PlatformStatistics;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 平台统计返回VO
 */
@Data
public class PlatformStatsVO {
    
    /**
     * 新增用户数（总和）
     */
    private Integer newUserCount;
    
    /**
     * 活跃用户数（总和）
     */
    private Integer activeUserCount;
    
    /**
     * 新增订单数（总和）
     */
    private Integer newOrderCount;
    
    /**
     * 总收入（总和）
     */
    private BigDecimal totalIncome;
    
    /**
     * 新增课程数（总和）
     */
    private Integer newCourseCount;
    
    /**
     * 视频播放总数（总和）
     */
    private Integer totalVideoViews;
    
    /**
     * 每日趋势数据
     */
    private List<PlatformStatistics> trendDaily;
}
