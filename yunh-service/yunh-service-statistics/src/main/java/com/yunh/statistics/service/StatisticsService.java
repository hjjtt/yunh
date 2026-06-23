package com.yunh.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.statistics.pojo.CourseStatistics;
import com.yunh.statistics.pojo.PlatformStatistics;
import com.yunh.statistics.pojo.vo.PlatformStatsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService extends IService<CourseStatistics> {
    
    List<CourseStatistics> getCourseStatistics(Long courseId, LocalDate startDate, LocalDate endDate);
    
    PlatformStatsVO getPlatformStatistics(LocalDate startDate, LocalDate endDate);
    
    void generateDailyStatistics();
}
