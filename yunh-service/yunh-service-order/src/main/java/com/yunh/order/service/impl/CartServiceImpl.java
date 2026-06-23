package com.yunh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.order.mapper.CartMapper;
import com.yunh.order.pojo.Cart;
import com.yunh.order.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    
    @Override
    public List<Cart> getByUserId(Long userId) {
        return list(new QueryWrapper<Cart>().eq("user_id", userId).orderByDesc("create_time"));
    }
    
    @Override
    public boolean add(Long userId, Long courseId) {
        Cart existCart = getOne(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("course_id", courseId));
        
        if (existCart != null) {
            log.info("课程已在购物车中，用户ID：{}，课程ID：{}", userId, courseId);
            return true;
        }
        
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCourseId(courseId);
        cart.setCreateTime(LocalDateTime.now());
        
        save(cart);
        log.info("添加到购物车成功，用户ID：{}，课程ID：{}", userId, courseId);
        return true;
    }
    
    @Override
    public boolean remove(Long userId, Long courseId) {
        boolean result = remove(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("course_id", courseId));
        
        log.info("从购物车移除，用户ID：{}，课程ID：{}，结果：{}", userId, courseId, result);
        return result;
    }
    
    @Override
    public boolean clear(Long userId) {
        boolean result = remove(new QueryWrapper<Cart>().eq("user_id", userId));
        
        log.info("清空购物车，用户ID：{}，结果：{}", userId, result);
        return result;
    }
}
