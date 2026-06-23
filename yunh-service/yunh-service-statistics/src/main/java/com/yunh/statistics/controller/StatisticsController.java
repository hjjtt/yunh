package com.yunh.statistics.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunh.common.result.Result;
import com.yunh.statistics.pojo.CourseStatistics;
import com.yunh.statistics.pojo.PlatformStatistics;
import com.yunh.statistics.pojo.vo.PlatformStatsVO;
import com.yunh.statistics.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计控制器
 */
@Api(tags = "数据统计")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @ApiOperation("获取课程统计")
    @GetMapping("/course/{courseId}")
    public Result<List<CourseStatistics>> getCourseStatistics(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(statisticsService.getCourseStatistics(courseId, startDate, endDate));
    }
    
    @ApiOperation("获取平台统计")
    @GetMapping("/platform")
    public Result<PlatformStatsVO> getPlatformStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PlatformStatsVO vo = statisticsService.getPlatformStatistics(startDate, endDate);
        return Result.success(vo);
    }
    
    @ApiOperation("生成每日统计")
    @PostMapping("/daily")
    public Result<Boolean> generateDailyStatistics() {
        statisticsService.generateDailyStatistics();
        return Result.success(true);
    }
}
