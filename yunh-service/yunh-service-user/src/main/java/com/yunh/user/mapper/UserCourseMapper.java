package com.yunh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.user.pojo.UserCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户选课记录 Mapper
 */
@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {
}
