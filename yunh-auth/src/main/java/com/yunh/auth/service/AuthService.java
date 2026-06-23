package com.yunh.auth.service;

import com.yunh.api.client.UserFeignClient;
import com.yunh.auth.config.CaptchaProperties;
import com.yunh.auth.util.JwtUtil;
import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.user.pojo.User;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    private static final String CLIENT_TYPE_ADMIN = "ADMIN";
    private static final String CLIENT_TYPE_MINI = "MINI";
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CaptchaProperties captchaProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserFeignClient userFeignClient;

    public Map<String, Object> login(String username, String password, String captchaKey, String captchaCode, String clientType) {
        if (!verifyCaptcha(captchaKey, captchaCode)) {
            throw new BusinessException("验证码错误或已过期");
        }

        Result<User> userResult = userFeignClient.getAuthByUsername(username);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new BusinessException("用户名或密码错误");
        }

        User user = userResult.getData();

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String normalizedClientType = normalizeClientType(clientType);
        validateClientAccess(user, normalizedClientType);

        String role = user.getRole() != null ? user.getRole() : "USER";
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), role);

        storeRefreshToken(user.getId(), refreshToken);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("createTime", user.getCreateTime());
        data.put("role", role);
        data.put("clientType", normalizedClientType);

        log.info("用户登录成功: userId={}, username={}, role={}, clientType={}",
                user.getId(), username, role, normalizedClientType);
        return data;
    }

    private String normalizeClientType(String clientType) {
        if (clientType == null || clientType.trim().isEmpty()) {
            return CLIENT_TYPE_ADMIN;
        }

        String normalized = clientType.trim().toUpperCase();
        if (CLIENT_TYPE_ADMIN.equals(normalized) || CLIENT_TYPE_MINI.equals(normalized)) {
            return normalized;
        }

        throw new BusinessException("不支持的登录端类型");
    }

    private void validateClientAccess(User user, String clientType) {
        if (CLIENT_TYPE_ADMIN.equals(clientType) && !"ADMIN".equals(user.getRole())) {
            throw new BusinessException("该账号无管理员权限，无法登录后台");
        }
    }

    public Map<String, String> generateCaptcha() {
        String key = CAPTCHA_KEY_PREFIX + UUID.randomUUID().toString().replace("-", "");
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < captchaProperties.getLength(); i++) {
            codeBuilder.append(random.nextInt(10));
        }
        String code = codeBuilder.toString();

        redisTemplate.opsForValue().set(key, code, captchaProperties.getExpireSeconds(), TimeUnit.SECONDS);

        // 生成图形验证码
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 22, 30);
        }

        // 添加干扰线
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }

        // 添加噪点
        for (int i = 0; i < 30; i++) {
            image.setRGB(random.nextInt(width), random.nextInt(height), random.nextInt(0xFFFFFF));
        }

        g.dispose();

        // 转为 Base64
        String captchaImage;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            captchaImage = javax.xml.bind.DatatypeConverter.printBase64Binary(baos.toByteArray());
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            throw new RuntimeException("生成验证码图片失败");
        }

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", captchaImage);
        log.debug("生成验证码: key={}", key);
        return result;
    }

    private boolean verifyCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        String storedCode = redisTemplate.opsForValue().get(captchaKey);
        if (storedCode == null) {
            return false;
        }
        redisTemplate.delete(captchaKey);
        return storedCode.equalsIgnoreCase(captchaCode);
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("RefreshToken 无效或已过期");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException("不是有效的 RefreshToken");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException("RefreshToken 已失效");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(userId, username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, role);

        storeRefreshToken(userId, newRefreshToken);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);

        log.info("Token 刷新成功: userId={}", userId);
        return data;
    }

    public void logout(String accessToken) {
        // 阶段 1：解析 JWT。如果 token 已过期或格式错误，无法用于认证，无需加黑名单
        Claims claims;
        try {
            claims = jwtUtil.parseToken(accessToken);
        } catch (Exception e) {
            log.info("登出时 JWT 解析失败（token 可能已过期），无需加入黑名单: {}", e.getMessage());
            return;
        }

        // 阶段 2：提取 userId
        Long userId = null;
        Object uid = claims.get("userId");
        if (uid instanceof Integer) {
            userId = ((Integer) uid).longValue();
        } else if (uid instanceof Long) {
            userId = (Long) uid;
        }

        // 阶段 3：将 accessToken 加入 Redis 黑名单
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            try {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + accessToken,
                        "1", ttl, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("写入 Token 黑名单失败，userId={}, error={}", userId, e.getMessage(), e);
                throw new RuntimeException("登出处理失败：Redis 操作异常", e);
            }
        }

        // 阶段 4：删除 refreshToken
        if (userId != null) {
            try {
                redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + userId);
            } catch (Exception e) {
                log.error("删除 RefreshToken 失败，userId={}, error={}", userId, e.getMessage(), e);
                throw new RuntimeException("登出处理失败：Redis 操作异常", e);
            }
        }

        log.info("用户登出成功: userId={}", userId);
    }

    public boolean isTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + accessToken));
    }

    private void storeRefreshToken(Long userId, String refreshToken) {
        long expireMs = jwtUtil.getRefreshTokenExpire();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + userId,
                refreshToken,
                expireMs,
                TimeUnit.MILLISECONDS);
    }
}
