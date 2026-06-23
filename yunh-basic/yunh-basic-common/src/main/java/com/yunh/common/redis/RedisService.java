package com.yunh.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 */
@Component
public class RedisService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 设置缓存
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置缓存（带过期时间)
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 获取缓存
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    
    /**
     * 判断缓存是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
    
    /**
     * 获取缓存过期时间
     */
    public long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }
    
    /**
     * 自增（原子操作）
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    /**
     * 自减（原子操作）
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
}
