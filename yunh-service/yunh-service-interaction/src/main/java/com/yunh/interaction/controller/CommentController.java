package com.yunh.interaction.controller;

import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.interaction.controller.support.UserHeaderSupport;
import com.yunh.interaction.pojo.Comment;
import com.yunh.interaction.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Api(tags = "评论管理")
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @ApiOperation("查询课程评论列表")
    @GetMapping("/course/{courseId}")
    public Result<List<Comment>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(commentService.getByCourseId(courseId));
    }
    
    @ApiOperation("创建评论")
    @PostMapping("/create")
    public Result<Comment> create(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                  @RequestBody Comment comment) {
        if (comment == null) {
            throw new BusinessException("评论内容不能为空");
        }
        if (comment.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        comment.setUserId(UserHeaderSupport.requireUserId(userIdHeader));
        comment.setContent(comment.getContent().trim());
        return Result.success(commentService.create(comment));
    }
    
    @ApiOperation("删除评论")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (!"ADMIN".equals(role) && !comment.getUserId().equals(UserHeaderSupport.requireUserId(userIdHeader))) {
            return Result.error("无权删除该评论");
        }
        return Result.success(commentService.removeById(id));
    }
}
