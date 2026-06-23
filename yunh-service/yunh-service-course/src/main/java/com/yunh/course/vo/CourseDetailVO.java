package com.yunh.course.vo;

import com.yunh.course.pojo.Course;
import com.yunh.user.vo.UserSafeVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 课程详情 VO（包含讲师信息）
 * 
 * VO (View Object)：视图对象，用于封装返回给前端的数据
 * 
 * @author yunh
 */
@Data
public class CourseDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程信息
     */
    private Course course;
    
    /**
     * 讲师信息（从 User 服务获取）
     */
    private UserSafeVO teacher;
}
