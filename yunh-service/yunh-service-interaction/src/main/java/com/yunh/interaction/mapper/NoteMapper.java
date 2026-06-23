package com.yunh.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.interaction.pojo.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记 Mapper 接口
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
