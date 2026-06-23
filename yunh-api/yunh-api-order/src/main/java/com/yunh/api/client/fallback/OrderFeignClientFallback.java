package com.yunh.api.client.fallback;

import com.yunh.api.client.OrderFeignClient;
import com.yunh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 订单服务 Feign 降级处理
 */
@Slf4j
@Component
public class OrderFeignClientFallback implements OrderFeignClient {
    
    @Override
    public Result getOrderById(Long orderId) {
        log.error("获取订单信息失败，订单ID：{}", orderId);
        return Result.error("订单服务不可用，请稍后重试");
    }
    
    @Override
    public Result getOrderByNo(String orderNo) {
        log.error("根据订单号获取订单失败，订单号：{}", orderNo);
        return Result.error("订单服务不可用，请稍后重试");
    }
    
    @Override
    public Result createOrder(Long userId, Long courseId) {
        log.error("创建订单失败，用户ID：{}，课程ID：{}", userId, courseId);
        return Result.error("订单服务不可用，请稍后重试");
    }
    
    @Override
    public Result cancelOrder(String orderNo) {
        log.error("取消订单失败，订单号：{}", orderNo);
        return Result.error("订单服务不可用，请稍后重试");
    }

    @Override
    public Result payOrder(String orderNo, Integer payType, BigDecimal payAmount) {
        log.error("支付订单失败，订单号：{}，支付方式：{}，支付金额：{}", orderNo, payType, payAmount);
        return Result.error("订单服务不可用，请稍后重试");
    }
    
    @Override
    public Result getOrdersByUserId(Long userId) {
        log.error("获取用户订单列表失败，用户ID：{}", userId);
        return Result.error("订单服务不可用，请稍后重试");
    }
}
