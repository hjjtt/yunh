package com.yunh.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.video.pojo.Video;

import java.util.List;

/**
 * 视频服务接口
 */
public interface VideoService extends IService<Video> {
    
    List<Video> getByCourseId(Long courseId);

    List<Video> getByChapterId(Long chapterId);
    
    Video upload(Video video);
    
    boolean incrementPlayCount(Long id);
}
