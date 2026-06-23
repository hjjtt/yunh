package com.yunh.interaction.controller;

import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.interaction.controller.support.UserHeaderSupport;
import com.yunh.interaction.pojo.Answer;
import com.yunh.interaction.pojo.Question;
import com.yunh.interaction.service.AnswerService;
import com.yunh.interaction.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回答控制器
 */
@Api(tags = "回答管理")
@RestController
@RequestMapping("/answer")
public class AnswerController {
    
    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;
    
    @ApiOperation("查询问题的回答列表")
    @GetMapping("/question/{questionId}")
    public Result<List<Answer>> getByQuestionId(@PathVariable Long questionId) {
        return Result.success(answerService.getByQuestionId(questionId));
    }
    
    @ApiOperation("创建回答")
    @PostMapping("/create")
    public Result<Answer> create(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                 @RequestBody Answer answer) {
        if (answer == null) {
            throw new BusinessException("回答内容不能为空");
        }
        if (answer.getQuestionId() == null) {
            throw new BusinessException("问题ID不能为空");
        }
        if (answer.getContent() == null || answer.getContent().trim().isEmpty()) {
            throw new BusinessException("回答内容不能为空");
        }

        answer.setUserId(UserHeaderSupport.requireUserId(userIdHeader));
        answer.setContent(answer.getContent().trim());
        return Result.success(answerService.create(answer));
    }
    
    @ApiOperation("采纳回答")
    @PostMapping("/accept/{id}")
    public Result<Boolean> accept(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Long currentUserId = UserHeaderSupport.requireUserId(userIdHeader);
        Answer answer = answerService.getById(id);
        if (answer == null) {
            return Result.error("回答不存在");
        }
        if (!"ADMIN".equals(role)) {
            // 只有问题的提问者才能采纳回答
            Question question = questionService.getById(answer.getQuestionId());
            if (question == null || !question.getUserId().equals(currentUserId)) {
                return Result.error("只有提问者才能采纳回答");
            }
        }
        return Result.success(answerService.acceptAnswer(id));
    }
}
