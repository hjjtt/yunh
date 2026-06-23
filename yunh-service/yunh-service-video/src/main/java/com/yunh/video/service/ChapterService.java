package com.yunh.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.video.pojo.Chapter;

import java.util.List;

/**
 * 章节服务接口
 */
public interface ChapterService extends IService<Chapter> {
    
    List<Chapter> getByCourseId(Long courseId);
    
    Chapter create(Chapter chapter);
}
