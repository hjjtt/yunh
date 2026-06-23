package com.yunh.video.controller;

import com.yunh.common.result.Result;
import com.yunh.video.pojo.Chapter;
import com.yunh.video.service.ChapterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节控制器
 */
@Api(tags = "章节管理")
@RestController
@RequestMapping("/chapter")
public class ChapterController {
    
    @Autowired
    private ChapterService chapterService;
    
    @ApiOperation("查询课程章节列表")
    @GetMapping("/course/{courseId}")
    public Result<List<Chapter>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(chapterService.getByCourseId(courseId));
    }
    
    @ApiOperation("创建章节")
    @PostMapping("/create")
    public Result<Chapter> create(@RequestBody Chapter chapter) {
        return Result.success(chapterService.create(chapter));
    }
    
    @ApiOperation("更新章节")
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Chapter chapter) {
        return Result.success(chapterService.updateById(chapter));
    }
    
    @ApiOperation("删除章节")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(chapterService.removeById(id));
    }
}
