package com.yunh.search.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程搜索 DTO
 */
@Data
public class CourseSearchDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String teacherName;
    
    private BigDecimal price;
    
    private String cover;
    
    private Integer status;
}
