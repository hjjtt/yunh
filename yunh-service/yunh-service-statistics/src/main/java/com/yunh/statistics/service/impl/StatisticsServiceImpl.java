package com.yunh.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.statistics.mapper.CourseStatisticsMapper;
import com.yunh.statistics.mapper.PlatformStatisticsMapper;
import com.yunh.statistics.pojo.CourseStatistics;
import com.yunh.statistics.pojo.PlatformStatistics;
import com.yunh.statistics.pojo.vo.PlatformStatsVO;
import com.yunh.statistics.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class StatisticsServiceImpl extends ServiceImpl<CourseStatisticsMapper, CourseStatistics> implements StatisticsService {
    
    @Autowired
    private PlatformStatisticsMapper platformStatisticsMapper;
    
    @Override
    public List<CourseStatistics> getCourseStatistics(Long courseId, LocalDate startDate, LocalDate endDate) {
        return list(new QueryWrapper<CourseStatistics>()
                .eq("course_id", courseId)
                .between("stat_date", startDate, endDate)
                .orderByDesc("stat_date"));
    }
    
    @Override
    public PlatformStatsVO getPlatformStatistics(LocalDate startDate, LocalDate endDate) {
        // 查询指定日期范围内的统计数据
        List<PlatformStatistics> list = platformStatisticsMapper.selectList(
                new QueryWrapper<PlatformStatistics>()
                        .between("stat_date", startDate, endDate)
                        .orderByAsc("stat_date")
        );
        
        // 计算汇总数据
        PlatformStatsVO vo = new PlatformStatsVO();
        vo.setTrendDaily(list);
        
        int newUserCount = 0;
        int activeUserCount = 0;
        int newOrderCount = 0;
        BigDecimal totalIncome = BigDecimal.ZERO;
        int newCourseCount = 0;
        int totalVideoViews = 0;
        
        for (PlatformStatistics stat : list) {
            newUserCount += stat.getNewUserCount() != null ? stat.getNewUserCount() : 0;
            activeUserCount += stat.getActiveUserCount() != null ? stat.getActiveUserCount() : 0;
            newOrderCount += stat.getNewOrderCount() != null ? stat.getNewOrderCount() : 0;
            totalIncome = totalIncome.add(stat.getTotalIncome() != null ? stat.getTotalIncome() : BigDecimal.ZERO);
            newCourseCount += stat.getNewCourseCount() != null ? stat.getNewCourseCount() : 0;
            totalVideoViews += stat.getTotalVideoViews() != null ? stat.getTotalVideoViews() : 0;
        }
        
        vo.setNewUserCount(newUserCount);
        vo.setActiveUserCount(activeUserCount);
        vo.setNewOrderCount(newOrderCount);
        vo.setTotalIncome(totalIncome);
        vo.setNewCourseCount(newCourseCount);
        vo.setTotalVideoViews(totalVideoViews);
        
        return vo;
    }
    
    @Override
    public void generateDailyStatistics() {
        log.info("生成每日统计数据");
        // TODO: 实现统计数据生成逻辑，查询用户、订单、课程、播放记录等数据生成每日统计
    }
}
