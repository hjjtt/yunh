package com.yunh.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunh.common.result.Result;
import com.yunh.pojo.dto.SelectCourseDTO;
import com.yunh.user.config.UserProperties;
import com.yunh.user.mapper.UserMapper;
import com.yunh.user.pojo.User;
import com.yunh.user.service.UserCourseService;
import com.yunh.user.vo.UserSafeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserProperties userProperties;

    @Autowired
    private UserCourseService userCourseService;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @ApiOperation(value = "用户登录", notes = "已迁移到 yunh-auth 认证服务，此接口仅保留兼容")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        return Result.error("请使用 /api/auth/login 接口登录");
    }

    @ApiOperation(value = "查询所有用户", notes = "获取系统中所有用户列表，仅管理员可访问")
    @GetMapping("/list")
    public Result<List<UserSafeVO>> list(
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!isAdmin(role)) {
            return Result.error(403, "仅管理员可查看用户列表");
        }
        List<User> users = userMapper.selectList(null);
        return Result.success(toSafeUsers(users));
    }

    @ApiOperation(value = "根据ID查询用户", notes = "查询指定用户的详细信息")
    @GetMapping("/{id}")
    public Result<UserSafeVO> getById(
            @ApiParam(value = "用户ID", example = "1") @PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toSafeUser(user));
    }

    @ApiOperation(value = "根据用户名查询", notes = "通过用户名精确查找用户")
    @GetMapping("/query")
    public Result<UserSafeVO> queryByUsername(
            @ApiParam(value = "用户名", example = "zhangsan") @RequestParam String username) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toSafeUser(user));
    }

    @ApiOperation(value = "内部鉴权查询用户", notes = "仅供认证服务校验用户名密码使用")
    @GetMapping("/internal/auth/username")
    public Result<User> getAuthUserByUsername(
            @ApiParam(value = "用户名", example = "zhangsan") @RequestParam String username) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @ApiOperation(value = "注册用户", notes = "新增用户，密码使用 BCrypt 加密")
    @PostMapping("/register")
    public Result<UserSafeVO> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        Long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setRole("USER");
        userMapper.insert(user);
        return Result.success(toSafeUser(user));
    }

    @ApiOperation(value = "更新用户信息", notes = "根据ID更新用户信息，仅管理员或本人可操作")
    @PutMapping("/{id}")
    public Result<UserSafeVO> updateUser(
            @ApiParam(value = "用户ID") @PathVariable Long id,
            @RequestBody User user,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!isAdmin(role) && !id.equals(parseUserId(userIdHeader))) {
            return Result.error("无权修改该用户信息");
        }
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        userMapper.updateById(user);
        return Result.success(toSafeUser(userMapper.selectById(id)));
    }

    @ApiOperation(value = "删除用户", notes = "根据ID删除用户，仅管理员可操作")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @ApiParam(value = "用户ID") @PathVariable Long id,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!isAdmin(role)) {
            return Result.error("无权删除用户");
        }
        int rows = userMapper.deleteById(id);
        if (rows > 0) {
            return Result.success();
        }
        return Result.error("用户不存在");
    }

    @ApiOperation(value = "健康检查", notes = "检查用户服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("user service is running");
    }

    @ApiOperation(value = "获取配置信息", notes = "获取 Nacos 动态配置，修改后无需重启即可生效")
    @GetMapping("/config")
    public Result<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("welcome", userProperties.getWelcome());
        config.put("version", userProperties.getVersion());
        config.put("message", "配置来自 Nacos，修改后无需重启即可生效");
        return Result.success(config);
    }

    @ApiOperation(value = "查询用户课程列表", notes = "演示 User 服务通过 Feign 调用 Course 服务")
    @GetMapping("/{id}/courses")
    public Result<List<Map<String, Object>>> getUserCourses(
            @ApiParam(value = "用户ID", example = "1") @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!isAdmin(role) && !id.equals(parseUserId(userIdHeader))) {
            return Result.error("无权查看该用户课程");
        }
        return userCourseService.getUserCourses(id);
    }

    @ApiOperation(value = "用户选课", notes = "分布式事务演示，包含扣减库存和创建订单")
    @PostMapping("/course/select")
    public Result<Void> selectCourse(
            @RequestBody @Validated SelectCourseDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long currentUserId = parseUserId(userIdHeader);
        if (currentUserId == null) {
            return Result.error("未获取到用户信息");
        }
        dto.setUserId(currentUserId);
        return userCourseService.selectCourse(dto);
    }

    @ApiOperation(value = "支付订单", notes = "用户支付选课订单")
    @PostMapping("/course/pay/{orderId}")
    public Result<Void> payOrder(
            @ApiParam(value = "订单ID", example = "1") @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!isAdmin(role)) {
            Long currentUserId = parseUserId(userIdHeader);
            if (currentUserId == null) {
                return Result.error("未获取到用户信息");
            }
            com.yunh.user.pojo.UserCourse order = userCourseService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (!currentUserId.equals(order.getUserId())) {
                return Result.error("无权操作该订单");
            }
        }
        return userCourseService.payOrder(orderId);
    }

    private UserSafeVO toSafeUser(User user) {
        return UserSafeVO.from(user);
    }

    private List<UserSafeVO> toSafeUsers(List<User> users) {
        return users.stream()
                .map(this::toSafeUser)
                .collect(Collectors.toList());
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }

    private Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
