package com.yunh.auth.controller;

import com.yunh.auth.service.AuthService;
import com.yunh.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "认证管理", description = "登录、验证码、Token 刷新等接口")
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation(value = "获取验证码", notes = "生成图形验证码，返回 captchaKey 和 Base64 图片")
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = authService.generateCaptcha();
        return Result.success(captcha);
    }

    @ApiOperation(value = "用户登录", notes = "通过用户名、密码和验证码登录，返回 accessToken 和 refreshToken")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String captchaKey = params.get("captchaKey");
        String captchaCode = params.get("captchaCode");
        String clientType = params.get("clientType");

        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (captchaKey == null || captchaCode == null) {
            return Result.error("请先获取验证码");
        }

        Map<String, Object> data = authService.login(username, password, captchaKey, captchaCode, clientType);
        return Result.success(data);
    }

    @ApiOperation(value = "刷新 Token", notes = "使用 refreshToken 获取新的 accessToken 和 refreshToken")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return Result.error("refreshToken 不能为空");
        }

        Map<String, Object> data = authService.refreshToken(refreshToken);
        return Result.success(data);
    }

    @ApiOperation(value = "用户登出", notes = "将 accessToken 加入黑名单并清除 refreshToken")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            authService.logout(token);
        } catch (RuntimeException e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error("登出处理异常，请稍后重试");
        }
        return Result.success();
    }

    @ApiOperation(value = "健康检查", notes = "检查认证服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("auth service is running");
    }
}
