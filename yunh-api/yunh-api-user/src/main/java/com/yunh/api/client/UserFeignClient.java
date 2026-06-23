package com.yunh.api.client;

import com.yunh.api.client.fallback.UserFeignClientFallback;
import com.yunh.common.result.Result;
import com.yunh.user.pojo.User;
import com.yunh.user.vo.UserSafeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端
 *
 * 学习要点：
 * 1. @FeignClient(name = "服务名") - 指定要调用的服务（必须与 Nacos 注册名一致）
 * 2. 接口方法定义与远程服务的 HTTP 接口完全对应
 * 3. 放在 api 模块中，避免循环依赖，方便服务间共享
 * 4. fallback 指定降级实现类，服务不可用时自动降级
 */
@FeignClient(name = "yunh-service-user", fallback = UserFeignClientFallback.class)
public interface UserFeignClient {

    /**
     * 根据 ID 查询用户
     * 对应 User 服务的 GET /user/{id} 接口
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/user/{id}")
    Result<UserSafeVO> getById(@PathVariable("id") Long id);

    /**
     * 根据用户名查询用户
     * 对应 User 服务的 GET /user/query?username=xxx 接口
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/user/query")
    Result<UserSafeVO> getByUsername(@RequestParam("username") String username);

    /**
     * 内部鉴权查询用户
     * 仅供认证服务读取密码哈希，不对外透出。
     *
     * @param username 用户名
     * @return 完整用户信息
     */
    @GetMapping("/user/internal/auth/username")
    Result<User> getAuthByUsername(@RequestParam("username") String username);
}
