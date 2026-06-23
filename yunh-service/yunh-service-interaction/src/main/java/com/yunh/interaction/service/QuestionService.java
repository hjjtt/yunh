package com.yunh.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.interaction.pojo.Question;

import java.util.List;

/**
 * 问答服务接口
 */
public interface QuestionService extends IService<Question> {
    
    List<Question> getByCourseId(Long courseId);
    
    Question create(Question question);
}
