package com.yunh.search.controller;

import com.yunh.common.result.Result;
import com.yunh.search.dto.CourseSearchDTO;
import com.yunh.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 搜索控制器
 */
@Api(tags = "课程搜索")
@RestController
@RequestMapping("/search")
public class SearchController {
    
    @Autowired
    private CourseSearchService courseSearchService;
    
    @ApiOperation("搜索课程")
    @GetMapping("/course")
    public Result<List<CourseSearchDTO>> searchCourse(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(courseSearchService.search(keyword, page, size));
    }
    
    @ApiOperation("同步课程索引")
    @PostMapping("/sync")
    public Result<Boolean> syncIndex() {
        courseSearchService.syncCourseIndex();
        return Result.success(true);
    }
    
    @ApiOperation("删除课程索引")
    @DeleteMapping("/course/{courseId}")
    public Result<Boolean> deleteIndex(@PathVariable Long courseId) {
        courseSearchService.deleteCourseIndex(courseId);
        return Result.success(true);
    }
}
