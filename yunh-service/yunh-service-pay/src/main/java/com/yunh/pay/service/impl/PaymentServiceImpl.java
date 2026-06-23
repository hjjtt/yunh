package com.yunh.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.api.client.OrderFeignClient;
import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.pay.mapper.PaymentMapper;
import com.yunh.pay.pojo.Payment;
import com.yunh.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final int ORDER_STATUS_PAID = 1;

    private final OrderFeignClient orderFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(String orderNo, BigDecimal amount, Integer payType, Long currentUserId) {
        // 幂等：已存在则直接返回
        Payment existingPayment = getByOrderNo(orderNo);
        if (existingPayment != null) {
            return existingPayment;
        }

        // 从订单服务获取订单信息，校验金额和归属
        Long orderUserId = resolveUserId(orderNo);
        BigDecimal orderAmount = resolveOrderAmount(orderNo);

        if (orderUserId == null) {
            throw new BusinessException("无法获取订单信息，支付创建失败");
        }

        // 归属校验：支付用户必须与订单用户一致（管理员不受限）
        if (currentUserId != null && !currentUserId.equals(orderUserId)) {
            throw new BusinessException("只能为自己的订单创建支付");
        }

        // 金额校验：前端传入金额必须与订单金额一致
        if (orderAmount != null && amount.compareTo(orderAmount) != 0) {
            log.warn("支付金额与订单金额不一致，orderNo={}, 前端传入={}, 订单金额={}",
                    orderNo, amount, orderAmount);
            throw new BusinessException("支付金额与订单金额不一致");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderNo(orderNo);
        payment.setUserId(orderUserId);
        payment.setAmount(orderAmount != null ? orderAmount : amount);
        payment.setPayType(payType);
        payment.setStatus(0);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        save(payment);
        log.info("支付单创建成功，支付单号：{}，订单号：{}，userId：{}", payment.getPaymentNo(), orderNo, orderUserId);
        return payment;
    }

    @Override
    public Payment getByOrderNo(String orderNo) {
        return getOne(new QueryWrapper<Payment>().eq("order_no", orderNo));
    }

    @Override
    public Payment getByPaymentNo(String paymentNo) {
        return getOne(new QueryWrapper<Payment>().eq("payment_no", paymentNo));
    }

    @Override
    public List<Payment> getByUserId(Long userId) {
        return list(new QueryWrapper<Payment>().eq("user_id", userId).orderByDesc("create_time"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleCallback(String paymentNo, String thirdPartyNo) {
        Payment payment = getOne(new QueryWrapper<Payment>().eq("payment_no", paymentNo));
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (payment.getStatus() != null && payment.getStatus() == 1) {
            return true;
        }

        payment.setStatus(1);
        payment.setThirdPartyNo(thirdPartyNo);
        payment.setPayTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        // 先持久化本地支付状态，再远程同步订单状态
        boolean updated = updateById(payment);
        if (!updated) {
            log.error("支付记录持久化失败，支付单号：{}", paymentNo);
            throw new BusinessException("支付记录更新失败");
        }

        if (!syncOrderStatus(payment)) {
            throw new BusinessException("支付状态同步订单失败，请稍后重试");
        }

        log.info("支付回调处理成功，支付单号：{}", paymentNo);
        return true;
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Long resolveUserId(String orderNo) {
        try {
            Result result = orderFeignClient.getOrderByNo(orderNo);
            if (result != null && result.isSuccess() && result.getData() instanceof Map) {
                Object userId = ((Map<?, ?>) result.getData()).get("userId");
                if (userId instanceof Number) {
                    return ((Number) userId).longValue();
                }
            }
        } catch (Exception e) {
            log.warn("解析订单用户失败，orderNo={}, error={}", orderNo, e.getMessage());
        }
        return null;
    }

    private BigDecimal resolveOrderAmount(String orderNo) {
        try {
            Result result = orderFeignClient.getOrderByNo(orderNo);
            if (result != null && result.isSuccess() && result.getData() instanceof Map) {
                return extractOrderAmount((Map<?, ?>) result.getData());
            }
        } catch (Exception e) {
            log.warn("解析订单金额失败，orderNo={}, error={}", orderNo, e.getMessage());
        }
        return null;
    }

    private boolean syncOrderStatus(Payment payment) {
        try {
            Result result = orderFeignClient.payOrder(payment.getOrderNo(), payment.getPayType(), payment.getAmount());
            if (result == null || !result.isSuccess()) {
                log.warn("支付同步订单返回失败，paymentNo={}, orderNo={}, message={}",
                        payment.getPaymentNo(),
                        payment.getOrderNo(),
                        result != null ? result.getMessage() : "返回结果为空");
                return confirmOrderPaid(payment);
            }
            return true;
        } catch (Exception e) {
            log.warn("支付同步订单状态异常，paymentNo={}, orderNo={}, error={}",
                    payment.getPaymentNo(), payment.getOrderNo(), e.getMessage());
            return confirmOrderPaid(payment);
        }
    }

    private boolean confirmOrderPaid(Payment payment) {
        try {
            Result result = orderFeignClient.getOrderByNo(payment.getOrderNo());
            if (result != null && result.isSuccess() && result.getData() instanceof Map) {
                Map<?, ?> orderData = (Map<?, ?>) result.getData();
                Integer orderStatus = extractOrderStatus(orderData);
                BigDecimal orderAmount = extractOrderAmount(orderData);
                if (orderStatus != null
                        && ORDER_STATUS_PAID == orderStatus
                        && (payment.getAmount() == null || orderAmount == null || payment.getAmount().compareTo(orderAmount) == 0)) {
                    log.info("订单状态二次确认成功，paymentNo={}, orderNo={}", payment.getPaymentNo(), payment.getOrderNo());
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("订单状态二次确认异常，paymentNo={}, orderNo={}, error={}",
                    payment.getPaymentNo(), payment.getOrderNo(), e.getMessage());
        }
        log.error("支付状态未能与订单状态确认一致，paymentNo={}, orderNo={}",
                payment.getPaymentNo(), payment.getOrderNo());
        return false;
    }

    private Integer extractOrderStatus(Map<?, ?> orderData) {
        Object status = orderData.get("status");
        if (status instanceof Number) {
            return ((Number) status).intValue();
        }
        if (status != null) {
            try {
                return Integer.parseInt(String.valueOf(status));
            } catch (NumberFormatException e) {
                log.warn("解析订单状态失败，status={}", status);
            }
        }
        return null;
    }

    private BigDecimal extractOrderAmount(Map<?, ?> orderData) {
        Object payAmount = orderData.get("payAmount");
        if (payAmount == null) {
            payAmount = orderData.get("totalAmount");
        }
        if (payAmount instanceof BigDecimal) {
            return (BigDecimal) payAmount;
        }
        if (payAmount instanceof Number) {
            return BigDecimal.valueOf(((Number) payAmount).doubleValue());
        }
        if (payAmount != null) {
            try {
                return new BigDecimal(String.valueOf(payAmount));
            } catch (NumberFormatException e) {
                log.warn("解析订单金额失败，payAmount={}", payAmount);
            }
        }
        return null;
    }
}
