package com.yunh.api.client.fallback;

import com.yunh.api.client.CourseFeignClient;
import com.yunh.common.result.Result;
import com.yunh.course.pojo.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * CourseFeignClient 降级实现
 *
 * 当 Course 服务不可用时，提供兜底数据
 * 确保调用方不会因为远程服务故障而崩溃
 */
@Slf4j
@Component
public class CourseFeignClientFallback implements CourseFeignClient {

    @Override
    public Result<Course> getById(Long id) {
        log.error("[降级] 获取课程详情失败，courseId: {}", id);
        return Result.error("课程服务暂不可用");
    }

    @Override
    public Result<List<Course>> getByTeacherId(Long teacherId) {
        log.error("[降级] 获取讲师课程列表失败，teacherId: {}", teacherId);
        return Result.error("课程服务暂不可用");
    }

    @Override
    public Result<List<Course>> list() {
        log.error("[降级] 获取课程列表失败");
        return Result.error("课程服务暂不可用");
    }

    @Override
    public Result<Void> decreaseStock(Long courseId, Integer count) {
        log.error("[降级] 扣减课程库存失败，courseId: {}, count: {}", courseId, count);
        return Result.error("课程服务暂不可用");
    }

    @Override
    public Result<Void> increaseStock(Long courseId, Integer count) {
        log.error("[降级] 恢复课程库存失败，courseId: {}, count: {}", courseId, count);
        return Result.error("课程服务暂不可用");
    }
}
