package com.yunh.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.interaction.pojo.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
