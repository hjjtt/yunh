package com.yunh.order.controller;

import com.yunh.common.result.Result;
import com.yunh.order.pojo.Cart;
import com.yunh.order.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Api(tags = "购物车管理")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("查询当前用户购物车")
    @GetMapping("/list")
    public Result<List<Cart>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(cartService.getByUserId(userId));
    }

    @ApiOperation("添加到购物车")
    @PostMapping("/add")
    public Result<Boolean> add(
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(cartService.add(userId, courseId));
    }

    @ApiOperation("从购物车移除")
    @DeleteMapping("/remove")
    public Result<Boolean> remove(
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(cartService.remove(userId, courseId));
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Boolean> clear(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(cartService.clear(userId));
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
