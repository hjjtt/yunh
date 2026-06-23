package com.yunh.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.video.pojo.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频 Mapper 接口
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
