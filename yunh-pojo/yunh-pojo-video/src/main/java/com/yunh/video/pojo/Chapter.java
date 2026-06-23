package com.yunh.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频章节实体类
 */
@Data
@TableName("t_chapter")
public class Chapter implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long courseId;
    
    private String title;
    
    private Integer sort;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
