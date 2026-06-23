package com.yunh.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.interaction.pojo.Comment;
import java.util.List;

public interface CommentService extends IService<Comment> {
    List<Comment> getByCourseId(Long courseId);
    Comment create(Comment comment);
}
