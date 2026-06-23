package com.yunh.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.interaction.mapper.QuestionMapper;
import com.yunh.interaction.pojo.Question;
import com.yunh.interaction.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
    @Override
    public List<Question> getByCourseId(Long courseId) {
        return list(new QueryWrapper<Question>()
                .eq("course_id", courseId)
                .orderByDesc("create_time"));
    }
    
    @Override
    public Question create(Question question) {
        question.setViewCount(0);
        question.setAnswerCount(0);
        question.setStatus(1);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        save(question);
        log.info("问题创建成功，用户ID：{}，课程ID：{}", question.getUserId(), question.getCourseId());
        return question;
    }
}
