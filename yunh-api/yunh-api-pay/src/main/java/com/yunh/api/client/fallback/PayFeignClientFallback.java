package com.yunh.api.client.fallback;

import com.yunh.api.client.PayFeignClient;
import com.yunh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付服务 Feign 降级处理
 */
@Slf4j
@Component
public class PayFeignClientFallback implements PayFeignClient {
    
    @Override
    public Result createPayment(String orderNo, BigDecimal amount, Integer payType) {
        log.error("创建支付记录失败，订单号：{}", orderNo);
        return Result.error("支付服务不可用，请稍后重试");
    }
    
    @Override
    public Result getPayStatus(String orderNo) {
        log.error("查询支付状态失败，订单号：{}", orderNo);
        return Result.error("支付服务不可用，请稍后重试");
    }
    
    @Override
    public Result refund(String orderNo, String reason) {
        log.error("退款失败，订单号：{}", orderNo);
        return Result.error("支付服务不可用，请稍后重试");
    }
}
