package com.yunh.pay.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 */
@Data
@TableName("t_refund")
public class Refund implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String refundNo;
    
    private String orderNo;
    
    private String paymentNo;
    
    private Long userId;
    
    private BigDecimal refundAmount;
    
    private String reason;
    
    private Integer status;
    
    private LocalDateTime refundTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
