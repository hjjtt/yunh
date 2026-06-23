package com.yunh.api.client;

import com.yunh.api.client.fallback.CourseFeignClientFallback;
import com.yunh.common.result.Result;
import com.yunh.course.pojo.Course;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 课程服务 Feign 客户端
 *
 * 学习要点：
 * 1. @FeignClient 的 name 必须与 Nacos 中的服务名完全一致
 * 2. @GetMapping 路径要与 CourseController 的路径完全匹配
 * 3. @PathVariable 必须指定 name 参数
 * 4. fallback 指定降级实现类，服务不可用时自动降级
 */
@FeignClient(name = "yunh-service-course", fallback = CourseFeignClientFallback.class)
public interface CourseFeignClient {

    /**
     * 根据 ID 获取课程详情
     * 对应 Course 服务的 GET /course/{id} 接口
     *
     * @param id 课程 ID
     * @return 课程信息
     */
    @GetMapping("/course/{id}")
    Result<Course> getById(@PathVariable("id") Long id);

    /**
     * 根据讲师 ID 获取课程列表
     * 对应 Course 服务的 GET /course/teacher/{teacherId} 接口
     *
     * @param teacherId 讲师 ID
     * @return 课程列表
     */
    @GetMapping("/course/teacher/{teacherId}")
    Result<List<Course>> getByTeacherId(@PathVariable("teacherId") Long teacherId);

    /**
     * 查询所有课程
     * 对应 Course 服务的 GET /course/list 接口
     *
     * @return 课程列表
     */
    @GetMapping("/course/list")
    Result<List<Course>> list();

    /**
     * 扣减课程库存（分布式事务调用）
     * 对应 Course 服务的 PUT /course/stock/decrease 接口
     *
     * @param courseId 课程 ID
     * @param count    扣减数量
     * @return 操作结果
     */
    @PutMapping("/course/stock/decrease")
    Result<Void> decreaseStock(@RequestParam("courseId") Long courseId, @RequestParam("count") Integer count);

    /**
     * 恢复课程库存（订单超时取消时调用）
     * 对应 Course 服务的 PUT /course/stock/increase 接口
     *
     * @param courseId 课程 ID
     * @param count    恢复数量
     * @return 操作结果
     */
    @PutMapping("/course/stock/increase")
    Result<Void> increaseStock(@RequestParam("courseId") Long courseId, @RequestParam("count") Integer count);
}
