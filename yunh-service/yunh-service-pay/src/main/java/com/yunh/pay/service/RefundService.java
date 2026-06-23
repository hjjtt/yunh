package com.yunh.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.pay.pojo.Refund;

/**
 * 退款服务接口
 */
public interface RefundService extends IService<Refund> {

    Refund applyRefund(String orderNo, String reason, Long operatorUserId);

    Refund getByOrderNo(String orderNo);

    boolean handleRefundCallback(String refundNo, boolean success);
}
