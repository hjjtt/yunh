package com.yunh.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.pay.pojo.Payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    Payment createPayment(String orderNo, BigDecimal amount, Integer payType, Long currentUserId);

    Payment getByOrderNo(String orderNo);

    Payment getByPaymentNo(String paymentNo);

    List<Payment> getByUserId(Long userId);

    boolean handleCallback(String paymentNo, String thirdPartyNo);
}
