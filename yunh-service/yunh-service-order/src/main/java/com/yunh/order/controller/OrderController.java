package com.yunh.order.controller;

import com.yunh.common.result.Result;
import com.yunh.order.pojo.Order;
import com.yunh.order.service.CartService;
import com.yunh.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
public class OrderController {

    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    private OrderService orderService;

    @ApiOperation("查询订单列表")
    @GetMapping("/list")
    public Result<List<Order>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (ROLE_ADMIN.equals(role)) {
            return Result.success(orderService.list());
        }
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(orderService.getByUserId(userId));
    }

    @ApiOperation("根据ID查询订单")
    @GetMapping("/{id}")
    public Result<Order> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!ROLE_ADMIN.equals(role) && !order.getUserId().equals(parseUserId(userIdHeader))) {
            return Result.error("无权查看该订单");
        }
        return Result.success(order);
    }

    @ApiOperation("根据订单号查询订单")
    @GetMapping("/no/{orderNo}")
    public Result<Order> getByOrderNo(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!ROLE_ADMIN.equals(role) && !order.getUserId().equals(parseUserId(userIdHeader))) {
            return Result.error("无权查看该订单");
        }
        return Result.success(order);
    }

    @ApiOperation("查询用户订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role) && !userId.equals(parseUserId(userIdHeader))) {
            return Result.error("无权查看该用户订单");
        }
        return Result.success(orderService.getByUserId(userId));
    }

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<Order> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam Long courseId) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(orderService.createOrder(userId, courseId));
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<Boolean> cancelOrder(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!ROLE_ADMIN.equals(role) && !order.getUserId().equals(parseUserId(userIdHeader))) {
            return Result.error("无权操作该订单");
        }
        return Result.success(orderService.cancelOrder(orderNo));
    }

    @ApiOperation("支付订单")
    @PostMapping("/pay/{orderNo}")
    public Result<Boolean> payOrder(
            @PathVariable String orderNo,
            @RequestParam Integer payType,
            @RequestParam(required = false) BigDecimal payAmount,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!ROLE_ADMIN.equals(role) && !order.getUserId().equals(parseUserId(userIdHeader))) {
            return Result.error("无权操作该订单");
        }
        return Result.success(orderService.payOrder(orderNo, payType, payAmount));
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
