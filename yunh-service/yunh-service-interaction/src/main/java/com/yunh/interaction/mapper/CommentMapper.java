package com.yunh.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.interaction.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
