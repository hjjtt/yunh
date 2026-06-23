package com.yunh.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.video.mapper.ChapterMapper;
import com.yunh.video.pojo.Chapter;
import com.yunh.video.service.ChapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {
    
    @Override
    public List<Chapter> getByCourseId(Long courseId) {
        return list(new QueryWrapper<Chapter>()
                .eq("course_id", courseId)
                .orderByAsc("sort"));
    }
    
    @Override
    public Chapter create(Chapter chapter) {
        chapter.setCreateTime(LocalDateTime.now());
        chapter.setUpdateTime(LocalDateTime.now());
        save(chapter);
        log.info("章节创建成功，标题：{}", chapter.getTitle());
        return chapter;
    }
}
