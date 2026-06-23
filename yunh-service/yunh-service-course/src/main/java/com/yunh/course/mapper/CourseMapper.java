package com.yunh.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.course.pojo.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程 Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 扣减库存（乐观锁方式，防止超卖）
     */
    @Update("UPDATE t_course SET stock = stock - #{count} WHERE id = #{courseId} AND stock >= #{count}")
    int decreaseStock(@Param("courseId") Long courseId, @Param("count") Integer count);

    /**
     * 恢复库存（订单超时取消时回滚库存）
     */
    @Update("UPDATE t_course SET stock = stock + #{count} WHERE id = #{courseId}")
    int increaseStock(@Param("courseId") Long courseId, @Param("count") Integer count);
}
