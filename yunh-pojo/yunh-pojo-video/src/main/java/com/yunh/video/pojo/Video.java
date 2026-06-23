package com.yunh.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Data
@TableName("t_video")
public class Video implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long courseId;
    
    private Long chapterId;
    
    private String title;
    
    private String description;
    
    private String videoUrl;
    
    private String coverUrl;
    
    private Integer duration;
    
    private Integer sort;
    
    private Integer status;
    
    private Integer playCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
