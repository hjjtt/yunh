package com.yunh.api.client;

import com.yunh.api.client.fallback.OrderFeignClientFallback;
import com.yunh.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 订单服务 Feign 客户端
 */
@FeignClient(name = "yunh-service-order", fallback = OrderFeignClientFallback.class)
public interface OrderFeignClient {
    
    @GetMapping("/order/{orderId}")
    Result getOrderById(@PathVariable("orderId") Long orderId);
    
    @GetMapping("/order/no/{orderNo}")
    Result getOrderByNo(@PathVariable("orderNo") String orderNo);
    
    @PostMapping("/order/create")
    Result createOrder(@RequestParam("userId") Long userId,
                       @RequestParam("courseId") Long courseId);
    
    @PostMapping("/order/cancel/{orderNo}")
    Result cancelOrder(@PathVariable("orderNo") String orderNo);

    @PostMapping("/order/pay/{orderNo}")
    Result payOrder(@PathVariable("orderNo") String orderNo,
                    @RequestParam("payType") Integer payType,
                    @RequestParam(value = "payAmount", required = false) BigDecimal payAmount);
    
    @GetMapping("/order/user/{userId}")
    Result getOrdersByUserId(@PathVariable("userId") Long userId);
}
