package com.yunh.order.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("t_order")
public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private String userName;
    
    private Long courseId;
    
    private String courseName;
    
    private BigDecimal originalPrice;
    
    private BigDecimal payAmount;
    
    private Integer status;
    
    private Integer payType;
    
    private String payTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
