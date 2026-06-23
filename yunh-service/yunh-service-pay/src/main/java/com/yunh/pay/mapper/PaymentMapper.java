package com.yunh.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.pay.pojo.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付 Mapper 接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
