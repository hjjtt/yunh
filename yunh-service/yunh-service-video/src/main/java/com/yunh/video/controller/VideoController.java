package com.yunh.video.controller;

import com.yunh.common.result.Result;
import com.yunh.video.pojo.Chapter;
import com.yunh.video.pojo.Video;
import com.yunh.video.service.ChapterService;
import com.yunh.video.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频控制器
 */
@Api(tags = "视频管理")
@RestController
@RequestMapping("/video")
public class VideoController {
    
    @Autowired
    private VideoService videoService;
    
    @ApiOperation("查询课程视频列表")
    @GetMapping("/course/{courseId}")
    public Result<List<Video>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(videoService.getByCourseId(courseId));
    }
    
    @ApiOperation("查询章节视频列表")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<Video>> getByChapterId(@PathVariable Long chapterId) {
        return Result.success(videoService.getByChapterId(chapterId));
    }

    @ApiOperation("根据ID查询视频")
    @GetMapping("/{id}")
    public Result<Video> getById(@PathVariable Long id) {
        return Result.success(videoService.getById(id));
    }
    
    @ApiOperation("上传视频")
    @PostMapping("/upload")
    public Result<Video> upload(@RequestBody Video video) {
        if (video.getCourseId() == null) {
            return Result.error("课程ID不能为空");
        }
        if (video.getTitle() == null || video.getTitle().trim().isEmpty()) {
            return Result.error("视频标题不能为空");
        }
        return Result.success(videoService.upload(video));
    }

    @ApiOperation("更新视频")
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Video video) {
        if (video.getId() == null) {
            return Result.error("视频ID不能为空");
        }
        return Result.success(videoService.updateById(video));
    }
    
    @ApiOperation("删除视频")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(videoService.removeById(id));
    }
    
    @ApiOperation("增加播放次数")
    @PostMapping("/play/{id}")
    public Result<Boolean> incrementPlayCount(@PathVariable Long id) {
        return Result.success(videoService.incrementPlayCount(id));
    }
}
