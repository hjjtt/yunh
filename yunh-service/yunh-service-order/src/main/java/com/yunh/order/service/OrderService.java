package com.yunh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.order.pojo.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    Order getByOrderNo(String orderNo);
    
    List<Order> getByUserId(Long userId);
    
    Order createOrder(Long userId, Long courseId);
    
    boolean cancelOrder(String orderNo);
    
    boolean payOrder(String orderNo, Integer payType, BigDecimal payAmount);
}
