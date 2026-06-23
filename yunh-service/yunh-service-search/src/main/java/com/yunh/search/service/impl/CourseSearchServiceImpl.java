package com.yunh.search.service.impl;

import com.yunh.search.dto.CourseSearchDTO;
import com.yunh.search.service.CourseSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程搜索服务实现类
 */
@Slf4j
@Service
public class CourseSearchServiceImpl implements CourseSearchService {
    
    @Override
    public List<CourseSearchDTO> search(String keyword, Integer page, Integer size) {
        log.info("搜索课程， 关键词：{}，页码：{}，大小：{}", keyword, page, size);
        return new ArrayList<>();
    }
    
    @Override
    public void syncCourseIndex() {
        log.info("同步课程索引到 Elasticsearch");
    }
    
    @Override
    public void deleteCourseIndex(Long courseId) {
        log.info("删除课程索引， 课程ID：{}", courseId);
    }
}
