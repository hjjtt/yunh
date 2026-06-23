package com.yunh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.api.client.CourseFeignClient;
import com.yunh.api.client.UserFeignClient;
import com.yunh.common.result.Result;
import com.yunh.course.pojo.Course;
import com.yunh.common.exception.BusinessException;
import com.yunh.order.mapper.OrderMapper;
import com.yunh.order.pojo.Order;
import com.yunh.order.service.OrderService;
import com.yunh.user.vo.UserSafeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CourseFeignClient courseFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long courseId) {
        UserSafeVO user = loadUser(userId);
        Course course = loadCourse(courseId);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setUserName(resolveUserName(user));
        order.setCourseId(courseId);
        order.setCourseName(course.getName());
        order.setOriginalPrice(defaultPrice(course.getPrice()));
        order.setPayAmount(defaultPrice(course.getPrice()));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        save(order);
        log.info("订单创建成功，订单号：{}, userId={}, courseId={}, courseName={}",
                order.getOrderNo(), userId, courseId, order.getCourseName());
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许取消");
        }
        order.setStatus(3);
        order.setUpdateTime(LocalDateTime.now());
        log.info("订单取消成功，订单号：{}", orderNo);
        return updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderNo, Integer payType, BigDecimal payAmount) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        if (payAmount != null && order.getPayAmount() != null && payAmount.compareTo(order.getPayAmount()) != 0) {
            throw new BusinessException("支付金额与订单应付金额不一致");
        }
        order.setStatus(1);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now().toString());
        order.setUpdateTime(LocalDateTime.now());
        log.info("订单支付成功，订单号：{}，支付方式：{}", orderNo, payType);
        return updateById(order);
    }
    
    @Override
    public Order getByOrderNo(String orderNo) {
        return getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
    }
    
    @Override
    public List<Order> getByUserId(Long userId) {
        return list(new QueryWrapper<Order>().eq("user_id", userId).orderByDesc("create_time"));
    }
    
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private UserSafeVO loadUser(Long userId) {
        Result<UserSafeVO> userResult = userFeignClient.getById(userId);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new BusinessException("用户不存在");
        }
        return userResult.getData();
    }

    private Course loadCourse(Long courseId) {
        Result<Course> courseResult = courseFeignClient.getById(courseId);
        if (courseResult == null || !courseResult.isSuccess() || courseResult.getData() == null) {
            throw new BusinessException("课程不存在");
        }
        return courseResult.getData();
    }

    private String resolveUserName(UserSafeVO user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname();
        }
        return user.getUsername();
    }

    private BigDecimal defaultPrice(BigDecimal price) {
        return price != null ? price : BigDecimal.ZERO;
    }
}
