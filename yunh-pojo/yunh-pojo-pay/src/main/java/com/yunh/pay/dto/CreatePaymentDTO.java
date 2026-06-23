package com.yunh.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求 DTO
 */
@Data
public class CreatePaymentDTO {

    /** 订单号 */
    private String orderNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式：1=微信，2=支付宝 */
    private Integer payType;
}
