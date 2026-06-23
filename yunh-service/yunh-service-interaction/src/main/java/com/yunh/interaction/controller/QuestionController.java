package com.yunh.interaction.controller;

import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.interaction.controller.support.UserHeaderSupport;
import com.yunh.interaction.pojo.Question;
import com.yunh.interaction.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 问答控制器
 */
@Api(tags = "问答管理")
@RestController
@RequestMapping("/question")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @ApiOperation("查询课程问答列表")
    @GetMapping("/course/{courseId}")
    public Result<List<Question>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(questionService.getByCourseId(courseId));
    }
    
    @ApiOperation("创建问题")
    @PostMapping("/create")
    public Result<Question> create(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @RequestBody Question question) {
        if (question == null) {
            throw new BusinessException("问题内容不能为空");
        }
        if (question.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
            throw new BusinessException("问题标题不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BusinessException("问题描述不能为空");
        }

        question.setUserId(UserHeaderSupport.requireUserId(userIdHeader));
        question.setTitle(question.getTitle().trim());
        question.setContent(question.getContent().trim());
        return Result.success(questionService.create(question));
    }
    
    @ApiOperation("删除问题")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("问题不存在");
        }
        if (!"ADMIN".equals(role) && !question.getUserId().equals(UserHeaderSupport.requireUserId(userIdHeader))) {
            return Result.error("无权删除该问题");
        }
        return Result.success(questionService.removeById(id));
    }
}
