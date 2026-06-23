package com.yunh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.order.pojo.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {
    
    List<Cart> getByUserId(Long userId);
    
    boolean add(Long userId, Long courseId);
    
    boolean remove(Long userId, Long courseId);
    
    boolean clear(Long userId);
}
