package com.yunh.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.common.exception.BusinessException;
import com.yunh.pay.mapper.RefundMapper;
import com.yunh.pay.pojo.Payment;
import com.yunh.pay.pojo.Refund;
import com.yunh.pay.service.PaymentService;
import com.yunh.pay.service.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Refund applyRefund(String orderNo, String reason, Long operatorUserId) {
        // 从支付记录获取关联信息
        Payment payment = paymentService.getByOrderNo(orderNo);

        Refund refund = new Refund();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderNo(orderNo);
        refund.setReason(reason);
        refund.setStatus(0);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        // 补全关键字段
        if (payment != null) {
            refund.setPaymentNo(payment.getPaymentNo());
            refund.setUserId(payment.getUserId());
            refund.setRefundAmount(payment.getAmount());
        } else {
            log.warn("退款申请未找到关联支付记录，orderNo={}", orderNo);
        }

        save(refund);
        log.info("退款申请成功，退款单号：{}，订单号：{}，退款金额：{}",
                refund.getRefundNo(), orderNo, refund.getRefundAmount());
        return refund;
    }

    @Override
    public Refund getByOrderNo(String orderNo) {
        return getOne(new QueryWrapper<Refund>().eq("order_no", orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefundCallback(String refundNo, boolean success) {
        Refund refund = getOne(new QueryWrapper<Refund>().eq("refund_no", refundNo));
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }

        refund.setStatus(success ? 1 : 2);
        refund.setRefundTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        log.info("退款回调处理成功，退款单号：{}，结果：{}", refundNo, success ? "成功" : "失败");
        return updateById(refund);
    }

    private String generateRefundNo() {
        return "REF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
