package com.yunh.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.common.exception.BusinessException;
import com.yunh.interaction.mapper.AnswerMapper;
import com.yunh.interaction.pojo.Answer;
import com.yunh.interaction.service.AnswerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {
    
    @Override
    public List<Answer> getByQuestionId(Long questionId) {
        return list(new QueryWrapper<Answer>()
                .eq("question_id", questionId)
                .orderByDesc("create_time"));
    }
    
    @Override
    public Answer create(Answer answer) {
        answer.setLikeCount(0);
        answer.setIsAccepted(0);
        answer.setStatus(1);
        answer.setCreateTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        save(answer);
        log.info("回答创建成功，问题ID：{}，用户ID：{}", answer.getQuestionId(), answer.getUserId());
        return answer;
    }
    
    @Override
    public boolean acceptAnswer(Long id) {
        Answer answer = getById(id);
        if (answer == null) {
            throw new BusinessException("回答不存在");
        }
        answer.setIsAccepted(1);
        answer.setUpdateTime(LocalDateTime.now());
        log.info("回答被采纳，回答ID：{}", id);
        return updateById(answer);
    }
}
