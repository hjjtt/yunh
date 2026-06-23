package com.yunh.common.exception;

import lombok.Data;

/**
 * 业务异常类
 */
@Data
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private Integer code;
    
    private String message;
    
    public BusinessException(String message) {
        this.message = message;
    }
    
    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
