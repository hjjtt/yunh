package com.yunh.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yunh.api.client.CourseFeignClient;
import com.yunh.common.constant.MqConstant;
import com.yunh.common.result.Result;
import com.yunh.course.pojo.Course;
import com.yunh.pojo.dto.CourseOrderMessage;
import com.yunh.pojo.dto.SelectCourseDTO;
import com.yunh.user.mapper.UserCourseMapper;
import com.yunh.user.pojo.UserCourse;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户课程服务
 *
 * 演示：User 服务通过 Feign 调用 Course 服务
 * 场景：查询用户的课程列表（跨服务调用）
 * 场景：用户选课（分布式事务）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseService {

    private final CourseFeignClient courseFeignClient;
    private final UserCourseMapper userCourseMapper;
    private final MqMessageService mqMessageService;

    /**
     * 用户选课（分布式事务 + MQ 消息通知 + 延迟消息超时检查）
     *
     * @GlobalTransactional 标注全局事务
     *                      - name: 事务名称（便于监控）
     *                      - rollbackFor: 哪些异常触发回滚
     *
     *                      业务流程：
     *                      1. 添加选课记录（本地事务 - User 服务）
     *                      2. 扣减课程库存（远程调用 - Course 服务）
     *                      3. 发送 MQ 消息（选课成功通知：短信、邮件、日志）
     *                      4. 发送延迟消息（30分钟后检查订单是否支付，未支付则自动取消）
     *                      如果步骤 2 失败，步骤 1 的数据也会自动回滚
     */
    @GlobalTransactional(name = "yunh-select-course", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> selectCourse(SelectCourseDTO dto) {
        Long userId = dto.getUserId();
        Long courseId = dto.getCourseId();

        log.info("====== 开始选课事务 ======");
        log.info("参数：userId={}, courseId={}, payType={}", userId, courseId, dto.getPayType());

        // 0. 检查是否已经选过该课程（避免唯一索引冲突）
        Long activeCount = userCourseMapper.selectCount(
                new QueryWrapper<UserCourse>()
                        .eq("user_id", userId)
                        .eq("course_id", courseId)
                        .ne("status", 2));
        if (activeCount > 0) {
            log.warn("用户已选过该课程，请勿重复选课：userId={}, courseId={}", userId, courseId);
            return Result.error("已选过该课程，请勿重复选课");
        }

        // 0.1 清理已取消的旧记录，允许重新选课（否则唯一索引冲突）
        userCourseMapper.delete(
                new QueryWrapper<UserCourse>()
                        .eq("user_id", userId)
                        .eq("course_id", courseId)
                        .eq("status", 2));

        // 1. 添加选课记录（本地事务）
        UserCourse userCourse = new UserCourse();
        userCourse.setUserId(userId);
        userCourse.setCourseId(courseId);
        userCourse.setStatus(0);
        userCourse.setPayType(dto.getPayType());
        userCourseMapper.insert(userCourse);

        log.info("选课记录已添加：id={}", userCourse.getId());

        // 2. 扣减课程库存（远程调用 Course 服务）
        // 如果此步骤失败，会触发全局回滚，步骤 1 的数据也会回滚
        Result<Void> stockResult = courseFeignClient.decreaseStock(courseId, 1);
        if (stockResult == null || !stockResult.isSuccess()) {
            log.error("扣减课程库存返回失败：courseId={}, result={}", courseId, stockResult);
            throw new RuntimeException("扣减课程库存失败：Course 服务返回错误");
        }

        log.info("课程库存已扣减：courseId={}", courseId);

        // 3. 发送选课成功 MQ 消息（异步通知：短信、邮件、日志）
        CourseOrderMessage message = new CourseOrderMessage();
        message.setOrderId(userCourse.getId());
        message.setUserId(userId);
        message.setCourseId(courseId);
        try {
            Result<Course> courseResult = courseFeignClient.getById(courseId);
            if (courseResult.isSuccess() && courseResult.getData() != null) {
                Course courseInfo = courseResult.getData();
                message.setCourseName(courseInfo.getName());
                message.setAmount(courseInfo.getPrice());
            } else {
                message.setCourseName("未知课程");
                message.setAmount(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.warn("获取课程信息失败，使用默认值：courseId={}, error={}", courseId, e.getMessage());
            message.setCourseName("未知课程");
            message.setAmount(BigDecimal.ZERO);
        }
        message.setStatus(0);
        message.setCreateTime(new Date());

        mqMessageService.sendCourseOrderSuccess(message);

        // 4. 发送延迟消息：30分钟后检查订单状态，未支付则自动取消
        mqMessageService.sendDelayMessage(
                MqConstant.TOPIC_COURSE_ORDER,
                MqConstant.TAG_ORDER_CANCEL,
                message,
                MqConstant.DELAY_LEVEL_ORDER_TIMEOUT);

        log.info("====== 选课事务完成（含 MQ 消息发送 + 延迟超时检查）======");

        return Result.success();
    }

    /**
     * 用户支付订单
     *
     * @param orderId 订单 ID（选课记录 ID）
     * @return 支付结果
     */
    /**
     * 根据 ID 查询选课记录
     */
    public UserCourse getOrderById(Long orderId) {
        return userCourseMapper.selectById(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> payOrder(Long orderId) {
        log.info("====== 开始支付订单 ======");
        log.info("参数：orderId={}", orderId);

        UserCourse userCourse = userCourseMapper.selectById(orderId);
        if (userCourse == null) {
            log.warn("订单不存在：orderId={}", orderId);
            return Result.error("订单不存在");
        }

        if (userCourse.getStatus() == 1) {
            log.warn("订单已支付，请勿重复支付：orderId={}", orderId);
            return Result.error("订单已支付，请勿重复支付");
        }

        if (userCourse.getStatus() == 2) {
            log.warn("订单已取消，无法支付：orderId={}", orderId);
            return Result.error("订单已取消，无法支付");
        }

        // 更新订单状态为已支付
        UserCourse update = new UserCourse();
        update.setId(orderId);
        update.setStatus(1);
        update.setUpdateTime(LocalDateTime.now());
        userCourseMapper.updateById(update);

        log.info("订单支付成功：orderId={}, userId={}, courseId={}",
                orderId, userCourse.getUserId(), userCourse.getCourseId());

        return Result.success();
    }

    /**
     * 订单超时取消（由延迟消息消费者调用）
     *
     * 逻辑：
     * 1. 查询订单状态
     * 2. 如果仍然是待支付（status=0），则取消订单
     * 3. 取消订单 = 更新状态为已取消（status=2） + 恢复课程库存
     *
     * @param orderId 订单 ID
     * @return 是否执行了取消操作
     */
    @GlobalTransactional(name = "yunh-cancel-timeout-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTimeoutOrder(Long orderId) {
        log.info("====== 检查订单是否超时未支付 ======");
        log.info("参数：orderId={}", orderId);

        UserCourse userCourse = userCourseMapper.selectById(orderId);
        if (userCourse == null) {
            log.warn("订单不存在，可能已被删除：orderId={}", orderId);
            return false;
        }

        if (userCourse.getStatus() != 0) {
            log.info("订单已处理，无需取消：orderId={}, status={}", orderId, userCourse.getStatus());
            return false;
        }

        // 取消订单：更新状态为已取消（2）
        UserCourse update = new UserCourse();
        update.setId(orderId);
        update.setStatus(2);
        update.setUpdateTime(LocalDateTime.now());
        userCourseMapper.updateById(update);

        // 恢复课程库存（远程调用 Course 服务）
        try {
            Result<Void> stockResult = courseFeignClient.increaseStock(userCourse.getCourseId(), 1);
            if (stockResult == null || !stockResult.isSuccess()) {
                log.error("恢复课程库存返回失败：courseId={}, result={}", userCourse.getCourseId(), stockResult);
                throw new RuntimeException("恢复课程库存失败：Course 服务返回错误");
            }
            log.info("课程库存已恢复：courseId={}", userCourse.getCourseId());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("恢复课程库存异常：courseId={}", userCourse.getCourseId(), e);
            throw new RuntimeException("恢复课程库存失败", e);
        }

        log.info("====== 订单超时取消完成：orderId={}, courseId={} ======", orderId, userCourse.getCourseId());
        return true;
    }

    /**
     * 获取用户选的课程列表（带课程详情）
     *
     * 业务流程：
     * 1. 查询用户选课关系（从数据库查询）
     * 2. 通过 Feign 调用 Course 服务获取课程详情
     * 3. 组装返回结果
     *
     * @param userId 用户 ID
     * @return 用户课程列表
     */
    public Result<List<Map<String, Object>>> getUserCourses(Long userId) {
        log.info("查询用户 {} 的课程列表", userId);

        List<Long> courseIds = findCourseIdsByUserId(userId);
        if (courseIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long courseId : courseIds) {
            try {
                Result<Course> courseResult = courseFeignClient.getById(courseId);
                if (courseResult.isSuccess() && courseResult.getData() != null) {
                    Course course = courseResult.getData();
                    Map<String, Object> item = new HashMap<>();
                    item.put("courseId", course.getId());
                    item.put("courseName", course.getName());
                    item.put("description", course.getDescription());
                    item.put("price", course.getPrice());
                    item.put("teacherName", course.getTeacherName());
                    result.add(item);
                }
            } catch (Exception e) {
                log.error("调用课程服务失败，courseId: {}, error: {}", courseId, e.getMessage());
                Map<String, Object> item = new HashMap<>();
                item.put("courseId", courseId);
                item.put("courseName", "课程暂不可用");
                result.add(item);
            }
        }

        log.info("查询到用户 {} 的 {} 门课程", userId, result.size());
        return Result.success(result);
    }

    /**
     * 查询用户选课关系（从数据库查询）
     */
    private List<Long> findCourseIdsByUserId(Long userId) {
        List<Long> courseIds = new ArrayList<>();
        List<UserCourse> userCourses = userCourseMapper.selectList(
                new QueryWrapper<UserCourse>().eq("user_id", userId));
        for (UserCourse uc : userCourses) {
            courseIds.add(uc.getCourseId());
        }
        return courseIds;
    }
}
