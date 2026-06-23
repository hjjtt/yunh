package com.yunh.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.video.mapper.VideoMapper;
import com.yunh.video.pojo.Video;
import com.yunh.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
    
    @Override
    public List<Video> getByCourseId(Long courseId) {
        return list(new QueryWrapper<Video>()
                .eq("course_id", courseId)
                .orderByAsc("sort"));
    }

    @Override
    public List<Video> getByChapterId(Long chapterId) {
        return list(new QueryWrapper<Video>()
                .eq("chapter_id", chapterId)
                .orderByAsc("sort"));
    }
    
    @Override
    public Video upload(Video video) {
        video.setPlayCount(0);
        video.setStatus(1);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());
        save(video);
        log.info("视频上传成功，标题：{}", video.getTitle());
        return video;
    }
    
    @Override
    public boolean incrementPlayCount(Long id) {
        Video video = getById(id);
        if (video == null) {
            return false;
        }
        video.setPlayCount(video.getPlayCount() + 1);
        video.setUpdateTime(LocalDateTime.now());
        return updateById(video);
    }
}
