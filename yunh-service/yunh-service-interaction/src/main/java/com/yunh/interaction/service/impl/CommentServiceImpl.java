package com.yunh.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.interaction.mapper.CommentMapper;
import com.yunh.interaction.pojo.Comment;
import com.yunh.interaction.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Override
    public List<Comment> getByCourseId(Long courseId) {
        return list(new QueryWrapper<Comment>()
                .eq("course_id", courseId)
                .orderByDesc("create_time"));
    }
    
    @Override
    public Comment create(Comment comment) {
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        save(comment);
        log.info("评论创建成功，用户ID：{}，课程ID：{}", comment.getUserId(), comment.getCourseId());
        return comment;
    }
}
