package com.yunh.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.interaction.pojo.Answer;

import java.util.List;

/**
 * 回答服务接口
 */
public interface AnswerService extends IService<Answer> {
    
    List<Answer> getByQuestionId(Long questionId);
    
    Answer create(Answer answer);
    
    boolean acceptAnswer(Long id);
}
