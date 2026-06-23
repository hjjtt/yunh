package com.yunh.pay.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@TableName("t_payment")
public class Payment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String paymentNo;
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal amount;
    
    private Integer payType;
    
    private Integer status;
    
    private String thirdPartyNo;
    
    private LocalDateTime payTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
