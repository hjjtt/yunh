package com.yunh.course.service.impl;

import com.yunh.course.mapper.CourseMapper;
import com.yunh.course.pojo.Course;
import com.yunh.course.service.CourseStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程库存服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseStockServiceImpl implements CourseStockService {

    private final CourseMapper courseMapper;

    /**
     * 扣减课程库存
     * 注意：不需要 @GlobalTransactional，由调用方（User 服务）发起全局事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseStock(Long courseId, Integer count) {
        log.info("开始扣减库存：courseId={}, count={}", courseId, count);

        if (courseId == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        if (count == null || count <= 0) {
            throw new RuntimeException("扣减数量必须大于0");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        if (course.getStock() < count) {
            throw new RuntimeException("库存不足");
        }

        int updated = courseMapper.decreaseStock(courseId, count);
        if (updated == 0) {
            throw new RuntimeException("扣减库存失败");
        }

        log.info("库存扣减成功：courseId={}, 剩余库存={}", courseId, course.getStock() - count);
    }

    /**
     * 恢复课程库存（订单超时取消时调用）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseStock(Long courseId, Integer count) {
        log.info("开始恢复库存：courseId={}, count={}", courseId, count);

        if (courseId == null) {
            throw new RuntimeException("课程ID不能为空");
        }
        if (count == null || count <= 0) {
            throw new RuntimeException("恢复数量必须大于0");
        }

        int updated = courseMapper.increaseStock(courseId, count);
        if (updated == 0) {
            throw new RuntimeException("恢复库存失败，课程不存在");
        }

        log.info("库存恢复成功：courseId={}, count={}", courseId, count);
    }
}
