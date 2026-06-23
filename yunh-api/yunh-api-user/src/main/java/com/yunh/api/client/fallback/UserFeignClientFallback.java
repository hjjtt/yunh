package com.yunh.api.client.fallback;

import com.yunh.api.client.UserFeignClient;
import com.yunh.common.result.Result;
import com.yunh.user.pojo.User;
import com.yunh.user.vo.UserSafeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserFeignClient 降级实现
 *
 * 当 User 服务不可用时，提供兜底数据
 * 确保调用方不会因为远程服务故障而崩溃
 */
@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public Result<UserSafeVO> getById(Long id) {
        log.error("[降级] 获取用户信息失败，userId: {}", id);
        return Result.error("用户服务暂不可用");
    }

    @Override
    public Result<UserSafeVO> getByUsername(String username) {
        log.error("[降级] 根据用户名查询失败，username: {}", username);
        return Result.error("用户服务暂不可用");
    }

    @Override
    public Result<User> getAuthByUsername(String username) {
        log.error("[降级] 内部鉴权查询失败，username: {}", username);
        return Result.error("用户服务暂不可用");
    }
}
