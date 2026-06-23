package com.yunh.api.client;

import com.yunh.api.client.fallback.PayFeignClientFallback;
import com.yunh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 支付服务 Feign 客户端
 */
@FeignClient(name = "yunh-service-pay", fallback = PayFeignClientFallback.class)
public interface PayFeignClient {
    
    @PostMapping("/pay/create")
    Result createPayment(@RequestParam("orderNo") String orderNo,
                         @RequestParam("amount") BigDecimal amount,
                         @RequestParam("payType") Integer payType);
    
    @GetMapping("/pay/status/{orderNo}")
    Result getPayStatus(@PathVariable("orderNo") String orderNo);
    
    @PostMapping("/pay/refund")
    Result refund(@RequestParam("orderNo") String orderNo,
                  @RequestParam("reason") String reason);
}
