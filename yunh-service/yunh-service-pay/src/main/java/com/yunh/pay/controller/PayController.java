package com.yunh.pay.controller;

import com.yunh.common.constant.InternalCallConstant;
import com.yunh.common.result.Result;
import com.yunh.pay.dto.CreatePaymentDTO;
import com.yunh.pay.dto.DemoPayDTO;
import com.yunh.pay.pojo.Payment;
import com.yunh.pay.pojo.Refund;
import com.yunh.pay.service.PaymentService;
import com.yunh.pay.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付控制器
 */
@Api(tags = "支付管理")
@RestController
@RequestMapping("/pay")
public class PayController {

    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundService refundService;

    @ApiOperation("创建支付")
    @PostMapping("/create")
    public Result<Payment> createPayment(
            @RequestBody CreatePaymentDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (dto.getOrderNo() == null || dto.getOrderNo().trim().isEmpty()) {
            return Result.error("订单号不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return Result.error("支付金额必须大于0");
        }
        if (dto.getPayType() == null) {
            return Result.error("支付方式不能为空");
        }
        Long currentUserId = parseUserId(userIdHeader);
        if (currentUserId == null && !ROLE_ADMIN.equals(role)) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(paymentService.createPayment(
                dto.getOrderNo(), dto.getAmount(), dto.getPayType(), currentUserId));
    }

    @ApiOperation("查询支付状态")
    @GetMapping("/status/{orderNo}")
    public Result<Payment> getPayStatus(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Payment payment = paymentService.getByOrderNo(orderNo);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        if (!ROLE_ADMIN.equals(role)) {
            Long currentUserId = parseUserId(userIdHeader);
            if (currentUserId == null || !currentUserId.equals(payment.getUserId())) {
                return Result.error("无权查看该支付记录");
            }
        }
        return Result.success(payment);
    }

    @ApiOperation("支付回调（仅限内部调用）")
    @PostMapping("/callback")
    public Result<Boolean> payCallback(
            @RequestParam String paymentNo,
            @RequestParam String thirdPartyNo,
            @RequestHeader(value = InternalCallConstant.HEADER_NAME, required = false) String internalCall) {
        if (!InternalCallConstant.HEADER_VALUE.equals(internalCall)) {
            return Result.error(403, "仅内部服务可调用支付回调");
        }
        return Result.success(paymentService.handleCallback(paymentNo, thirdPartyNo));
    }

    @ApiOperation("演示支付（供小程序模拟支付完成）")
    @PostMapping("/demo-pay")
    public Result<Boolean> demoPay(
            @RequestBody DemoPayDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (dto == null || dto.getPaymentNo() == null || dto.getPaymentNo().trim().isEmpty()) {
            return Result.error("支付单号不能为空");
        }
        String paymentNo = dto.getPaymentNo().trim();
        // 查找支付记录并校验归属
        Payment payment = paymentService.getByPaymentNo(paymentNo);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return Result.success(true);
        }
        if (!ROLE_ADMIN.equals(role)) {
            Long currentUserId = parseUserId(userIdHeader);
            if (currentUserId == null || !currentUserId.equals(payment.getUserId())) {
                return Result.error("无权操作该支付记录");
            }
        }
        String thirdPartyNo = "WX-DEMO-" + System.currentTimeMillis();
        return Result.success(paymentService.handleCallback(paymentNo, thirdPartyNo));
    }

    @ApiOperation("查询支付记录列表")
    @GetMapping("/list")
    public Result<List<Payment>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (ROLE_ADMIN.equals(role)) {
            return Result.success(paymentService.list());
        }
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return Result.error("未获取到用户信息");
        }
        return Result.success(paymentService.getByUserId(userId));
    }

    @ApiOperation("申请退款（仅管理员）")
    @PostMapping("/refund")
    public Result<Refund> refund(
            @RequestParam String orderNo,
            @RequestParam String reason,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            return Result.error("仅管理员可操作退款");
        }
        return Result.success(refundService.applyRefund(orderNo, reason, parseUserId(userIdHeader)));
    }

    @ApiOperation("查询退款记录（仅管理员）")
    @GetMapping("/refund/{orderNo}")
    public Result<Refund> getRefund(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!ROLE_ADMIN.equals(role)) {
            return Result.error("仅管理员可查看退款记录");
        }
        Refund refund = refundService.getByOrderNo(orderNo);
        if (refund == null) {
            return Result.error("退款记录不存在");
        }
        return Result.success(refund);
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
