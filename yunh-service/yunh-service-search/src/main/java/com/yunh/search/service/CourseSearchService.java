package com.yunh.search.service;

import com.yunh.search.dto.CourseSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程搜索服务接口
 */
public interface CourseSearchService {
    
    List<CourseSearchDTO> search(String keyword, Integer page, Integer size);
    
    void syncCourseIndex();
    
    void deleteCourseIndex(Long courseId);
}
